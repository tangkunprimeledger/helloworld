package com.higgs.trust.rs.core.callback;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.DistributeCallbackNotifyService;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxProcessRepository;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.SlaveBatchCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
@Component
@Slf4j
public class SlaveBatchCallbackProcessor implements SlaveBatchCallbackHandler, InitializingBean {
    private static final String KEY_ALL = "ALL";
    private static final String KEY_SELF = "SELF";
    private static final String KEY_OTHER = "OTHER";

    @Autowired
    private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired
    private CoreTxRepository coreTxRepository;
    @Autowired
    private RsCoreBatchCallbackProcessor rsCoreBatchCallbackProcessor;
    @Autowired
    private VoteRuleRepository voteRuleRepository;
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private DistributeCallbackNotifyService distributeCallbackNotifyService;

    @Override
    public void afterPropertiesSet() throws Exception {
        slaveCallbackRegistor.registBatchCallbackHandler(this);
    }

    @Override
    public void onPersisted(List<SignedTransaction> txs, Map<String, TransactionReceipt> txReceiptMap, BlockHeader blockHeader) {
        Profiler.enter("[rc.core.parseTxs]");
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceiptMap);
        Profiler.release();
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        List<RsCoreTxVO> selfTxs = map.get(KEY_SELF);
        List<RsCoreTxVO> otherTxs = map.get(KEY_OTHER);
        // allTxs = selfTxs + otherTxs, except callback for self and this node is not the sender
        if (CollectionUtils.isEmpty(allTxs)) {
            log.warn("[onPersisted]allTxs is empty,blockHeight:{}", blockHeader.getHeight());
            return;
        }
        boolean needCallbackCustom = false;
        //sendByOther
        if (!CollectionUtils.isEmpty(otherTxs)) {
            log.debug("[onPersisted]batchInsert.coreTx coreTxProcess,blockHeight:{}", blockHeader.getHeight());
            batchInsert(otherTxs, blockHeader.getHeight(), CoreTxStatusEnum.PERSISTED);
            needCallbackCustom = true;
        }
        //sendBySelf
        if (!CollectionUtils.isEmpty(selfTxs)) {
            log.debug("[onPersisted]batchUpdate.coreTx,blockHeight:{}", blockHeader.getHeight());
            try {
                batchUpdate(selfTxs, blockHeader.getHeight(), CoreTxStatusEnum.WAIT, CoreTxStatusEnum.PERSISTED);
                needCallbackCustom = true;
            } catch (RsCoreException e) {
                if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED != e.getCode() && RsCoreErrorEnum.RS_CORE_TX_UPDATE_FAILED != e.getCode()) {
                    throw e;
                }
                log.warn("[onPersisted]callback self batchUpdateStatus is fail (core_tx is not exist or status not WAIT), blockHeight:{}", blockHeader.getHeight());
                log.warn("onPersisted]try batchInsert.coreTx,status=PERSISTED,blockHeight:{}", blockHeader.getHeight());
                batchInsert(selfTxs, blockHeader.getHeight(), CoreTxStatusEnum.PERSISTED);
                needCallbackCustom = true;
            }
        }
        //callback custom rs
        if (needCallbackCustom) {
            Profiler.enter("[rc.core.callbackCustom]");
            callbackCustom(allTxs, blockHeader, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY, true);
            Profiler.release();
        }
    }


    @Override
    public void onClusterPersisted(List<SignedTransaction> txs, Map<String, TransactionReceipt> txReceiptMap, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceiptMap);
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        if (CollectionUtils.isEmpty(allTxs)) {
            log.warn("[onClusterPersisted]allTxs is empty,blockHeight:{}", blockHeader.getHeight());
            return;
        }
        log.debug("[onClusterPersisted]batchUpdate.coreTx,blockHeight:{}", blockHeader.getHeight());
        boolean needCallbackCustom = false;
        try {
            batchUpdate(allTxs, blockHeader.getHeight(), CoreTxStatusEnum.PERSISTED, CoreTxStatusEnum.END);
            needCallbackCustom = true;
        } catch (RsCoreException e) {
            //when rows has been deleted by task , this exception will appear
            if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED != e.getCode()) {
                throw e;
            }
        }
        //callback custom rs
        if (needCallbackCustom) {
            callbackCustom(allTxs, blockHeader, RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY, false);
        }
    }

    /**
     * @param allTxs
     * @param blockHeader
     * @param redisMegGroupEnum
     * @param isOnPersisted     1. true for onPersisted  2. false for onClusterPersisted
     */
    private void callbackCustom(List<RsCoreTxVO> allTxs, BlockHeader blockHeader, RedisMegGroupEnum redisMegGroupEnum, boolean isOnPersisted) {
        if (isOnPersisted) {
            Profiler.enter("[rc.core.onPersisted]");
            rsCoreBatchCallbackProcessor.onPersisted(allTxs, blockHeader);
            Profiler.release();
        } else {
            rsCoreBatchCallbackProcessor.onEnd(allTxs, blockHeader);
        }
        //sync notify
        List<RespData<String>> respDatas = new ArrayList<>(allTxs.size());
        for (RsCoreTxVO tx : allTxs) {
            try {
                RespData<String> respData = new RespData<>();
                if (CoreTxResultEnum.SUCCESS != tx.getExecuteResult()) {
                    respData.setCode(tx.getErrorCode());
                    respData.setMsg(tx.getErrorMsg());
                }
                respData.setData(tx.getTxId());
                respDatas.add(respData);
            } catch (Throwable e) {
                log.warn("[callbackCustom]sync notify rs resp data failed", e);
            }
        }
        Profiler.enter("[rc.core.notifySyncResult]");
        distributeCallbackNotifyService.notifySyncResult(respDatas, redisMegGroupEnum);
        Profiler.release();
    }

    @Override
    public void onFailover(List<SignedTransaction> txs, Map<String, TransactionReceipt> txReceiptMap, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceiptMap);
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        if (CollectionUtils.isEmpty(allTxs)) {
            log.warn("[onFailover]allTxs is empty,blockHeight:{}", blockHeader.getHeight());
        }
        log.info("[onFailover]batchInsert.coreTx,blockHeight:{}", blockHeader.getHeight());
        if (rsConfig.isUseMySQL()) {
            try {
                batchInsert(allTxs, blockHeader.getHeight(), CoreTxStatusEnum.END);
            }catch (RsCoreException e){
                //数据库中可能还有原业务数据，当单个节点跟集群区块不一致时，
                //需要恢复差异的数据，本节点未做完的交易可能会再failover回来.
                if(e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT){
                    //process for each
                    for(RsCoreTxVO tx : allTxs) {
                        boolean isExist = coreTxRepository.isExist(tx.getTxId());
                        if (isExist) {
                            CoreTransactionProcessPO processPO = coreTxProcessRepository.queryByTxId(tx.getTxId(),false);
                            if(processPO != null){
                                coreTxProcessRepository.updateStatus(tx.getTxId(),CoreTxStatusEnum.formCode(processPO.getStatus()),CoreTxStatusEnum.END);
                            }
                        } else {
                            coreTxRepository.add(coreTxRepository.convertTxVO(tx), tx.getSignDatas(), blockHeader.getHeight());
                        }
                    }
                }else {
                    throw e;
                }
            }
        } else {
            coreTxRepository.failoverBatchInsert(allTxs, blockHeader.getHeight());
        }
        rsCoreBatchCallbackProcessor.onFailover(allTxs, blockHeader);
    }

    /**
     * parse txs
     *
     * @param txs
     * @param txReceiptMap
     * @return
     */
    private Map<String, List<RsCoreTxVO>> parseTxs(List<SignedTransaction> txs, Map<String, TransactionReceipt> txReceiptMap) {
        Map<String, List<RsCoreTxVO>> map = new HashMap<>();
        List<RsCoreTxVO> allList = new ArrayList<>();
        List<RsCoreTxVO> selfList = new ArrayList<>();
        List<RsCoreTxVO> otherList = new ArrayList<>();
        for (SignedTransaction tx : txs) {
            String sender = tx.getCoreTx().getSender();
            CallbackTypeEnum callbackType = getCallbackType(tx.getCoreTx());
            if (callbackType == CallbackTypeEnum.SELF && !sendBySelf(sender)) {
                log.debug("[onClusterPersisted]only call self");
                continue;
            }
            CoreTransaction coreTx = tx.getCoreTx();
            RsCoreTxVO vo = BeanConvertor.convertBean(coreTx, RsCoreTxVO.class);
            vo.setSignDatas(tx.getSignatureList());
            vo.setVersion(VersionEnum.getBizTypeEnumBycode(coreTx.getVersion()));
            TransactionReceipt receipt = txReceiptMap.get(coreTx.getTxId());

            if (null != receipt) {
                SlaveErrorEnum slaveErrorEnum = SlaveErrorEnum.getByCode(receipt.getErrorCode());
                if (slaveErrorEnum != null) {
                    vo.setErrorCode(receipt.getErrorCode());
                    vo.setErrorMsg(slaveErrorEnum.getDescription());
                }
                vo.setExecuteResult(receipt.isResult() ? CoreTxResultEnum.SUCCESS : CoreTxResultEnum.FAIL);
            }

            if (!sendBySelf(coreTx.getSender())) {
                otherList.add(vo);
            } else {
                selfList.add(vo);
            }
            allList.add(vo);
        }
        map.put(KEY_ALL, allList);
        map.put(KEY_SELF, selfList);
        map.put(KEY_OTHER, otherList);
        return map;
    }


    /**
     * get callback type from policy
     *
     * @param tx
     * @return
     */
    private CallbackTypeEnum getCallbackType(CoreTransaction tx) {
        String policyId = tx.getPolicyId();
        //default callback ALL
        CallbackTypeEnum callbackType = CallbackTypeEnum.ALL;
        //get from default
        InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (policyEnum == null) {
            //from db
            VoteRule voteRule = voteRuleRepository.queryByPolicyId(policyId);
            if (voteRule == null) {
                log.error("[getCallbackType]getVoteRule is empty by policyId:{},txId:{}", policyId, tx.getTxId());
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_VOTE_RULE_NOT_EXISTS_ERROR);
            }
            callbackType = voteRule.getCallbackType();
        }
        log.debug("[getCallbackType]callbackType:{}", callbackType);
        return callbackType;
    }

    /**
     * @param sender
     * @return
     */
    private boolean sendBySelf(String sender) {
        if (StringUtils.equals(rsConfig.getRsName(), sender)) {
            return true;
        }
        return false;
    }

    /**
     * batch insert coreTx and coreTxProcess rows
     *
     * @param txs
     * @param height
     * @param statusEnum
     */
    private void batchInsert(List<RsCoreTxVO> txs, Long height, CoreTxStatusEnum statusEnum) {
        //insert coreTx
        coreTxRepository.batchInsert(txs, height, statusEnum);
    }

    /**
     * batch update coreTx and coreTxProcess
     *
     * @param txs
     * @param height
     * @param from
     * @param to
     */
    private void batchUpdate(List<RsCoreTxVO> txs, Long height, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        //only on  persisted update coreTx
        if (to == CoreTxStatusEnum.PERSISTED) {
            coreTxRepository.batchUpdate(txs, height);
        }
        //update coreTxProcess status
        coreTxRepository.batchUpdateStatus(txs, from, to, height);
    }
}
