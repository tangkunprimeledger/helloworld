package com.higgs.trust.rs.core.callback;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.SlaveBatchCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.util.asynctosync.HashBlockingMap;
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
@Component @Slf4j public class SlaveBatchCallbackProcessor implements SlaveBatchCallbackHandler, InitializingBean {
    private static final String KEY_ALL = "ALL";
    private static final String KEY_SELF = "SELF";
    private static final String KEY_OTHER = "OTHER";

    @Autowired private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired private CoreTxRepository coreTxRepository;
    @Autowired private RsCoreBatchCallbackProcessor rsCoreBatchCallbackProcessor;
    @Autowired private VoteRuleRepository voteRuleRepository;
    @Autowired private HashBlockingMap<RespData> persistedResultMap;
    @Autowired private HashBlockingMap<RespData> clusterPersistedResultMap;
    @Autowired private RsConfig rsConfig;

    @Override public void afterPropertiesSet() throws Exception {
        slaveCallbackRegistor.registBatchCallbackHandler(this);
    }

    @Override
    public void onPersisted(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceipts);
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        List<RsCoreTxVO> selfTxs = map.get(KEY_SELF);
        List<RsCoreTxVO> otherTxs = map.get(KEY_OTHER);
        boolean callbackCustom = false;
        //sendByOther
        if (!CollectionUtils.isEmpty(otherTxs)) {
            log.info("[onPersisted]batchInsert.coreTx,blockHeight:{}", blockHeader.getHeight());
            coreTxRepository.batchInsert(otherTxs, CoreTxStatusEnum.END, blockHeader.getHeight());
            callbackCustom = true;
        }
        //sendBySelf
        if (!CollectionUtils.isEmpty(selfTxs)) {
            log.info("[onPersisted]batchUpdate.coreTx,blockHeight:{}", blockHeader.getHeight());
            try {
                coreTxRepository.batchUpdateStatus(selfTxs, CoreTxStatusEnum.WAIT, CoreTxStatusEnum.PERSISTED,
                    blockHeader.getHeight());
                callbackCustom = true;
            } catch (RsCoreException e) {
                if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED == e.getCode()) {
                    log.warn(
                        "onPersisted]callback self batchUpdateStatus is fail (core_tx is not exist or status not WAIT), blockHeight:{}",
                        blockHeader.getHeight());
                    log.warn("onPersisted]try batchInsert.coreTx,status=PERSISTED,blockHeight:{}",
                        blockHeader.getHeight());
                    coreTxRepository.batchInsert(selfTxs, CoreTxStatusEnum.PERSISTED, blockHeader.getHeight());
                    return;
                }
                throw e;
            }
        }
        //callback custom rs
        if (callbackCustom) {
            rsCoreBatchCallbackProcessor.onPersisted(allTxs,blockHeader);
        }
        //同步通知
        for (RsCoreTxVO tx : allTxs) {
            try {
                RespData respData = new RespData();
                respData.setCode(tx.getErrorCode());
                respData.setMsg(tx.getErrorMsg());
                persistedResultMap.put(tx.getTxId(), respData);
            } catch (Throwable e) {
                log.warn("[onPersisted]sync notify rs resp data failed", e);
            }
        }
    }

    @Override public void onClusterPersisted(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,
        BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceipts);
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        boolean callbackCustom = false;
        if (!CollectionUtils.isEmpty(allTxs)) {
            log.info("[onClusterPersisted]batchUpdate.coreTx,blockHeight:{}", blockHeader.getHeight());
            try {
                coreTxRepository.batchUpdateStatus(allTxs, CoreTxStatusEnum.PERSISTED, CoreTxStatusEnum.END,
                    blockHeader.getHeight());
                callbackCustom = true;
            } catch (RsCoreException e) {
                if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED != e.getCode()) {
                    throw e;
                }
                callbackCustom = processEndForEach(allTxs,false,blockHeader.getHeight());
            }
        }
        if (callbackCustom) {
            rsCoreBatchCallbackProcessor.onEnd(allTxs,blockHeader);
        }
        //同步通知
        for (RsCoreTxVO tx : allTxs) {
            try {
                RespData respData = new RespData();
                respData.setCode(tx.getErrorCode());
                respData.setMsg(tx.getErrorMsg());
                clusterPersistedResultMap.put(tx.getTxId(), respData);
            } catch (Throwable e) {
                log.warn("[onClusterPersisted]sync notify rs resp data failed", e);
            }
        }
    }

    @Override
    public void onFailover(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTxs(txs, txReceipts);
        List<RsCoreTxVO> allTxs = map.get(KEY_ALL);
        boolean callbackCustom = false;
        if (!CollectionUtils.isEmpty(allTxs)) {
            log.info("[onFailover]batchInsert.coreTx,blockHeight:{}", blockHeader.getHeight());
            try {
                coreTxRepository.batchInsert(allTxs, CoreTxStatusEnum.END, blockHeader.getHeight());
            }catch (RsCoreException e){
                if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED != e.getCode()) {
                    throw e;
                }
                callbackCustom = processEndForEach(allTxs,true,blockHeader.getHeight());
            }
        }
        if(callbackCustom){
            rsCoreBatchCallbackProcessor.onFailover(allTxs,blockHeader);
        }
    }

    /**
     * parse txs
     *
     * @param txs
     * @param txReceipts
     * @return
     */
    private Map<String, List<RsCoreTxVO>> parseTxs(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts) {
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
            for (TransactionReceipt receipt : txReceipts) {
                if (StringUtils.equals(receipt.getTxId(), coreTx.getTxId())) {
                    SlaveErrorEnum slaveErrorEnum = SlaveErrorEnum.getByCode(receipt.getErrorCode());
                    if (slaveErrorEnum != null) {
                        vo.setErrorCode(receipt.getErrorCode());
                        vo.setErrorMsg(slaveErrorEnum.getDescription());
                    }
                    vo.setExecuteResult(receipt.isResult() ? CoreTxResultEnum.SUCCESS : CoreTxResultEnum.FAIL);
                    break;
                }
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
     * process end status for each
     *
     * @param txs
     * @param isfailOver
     * @param blockHeight
     * @return
     */
    private boolean processEndForEach(List<RsCoreTxVO> txs,boolean isfailOver,Long blockHeight) {
        int num = 0;
        for (RsCoreTxVO tx : txs) {
            CoreTransactionPO coreTransactionPO = coreTxRepository.queryByTxId(tx.getTxId(), false);
            if (coreTransactionPO == null) {
                log.info("[processEndForEach]coreTransactionPO is null so add id,txId:{}", tx.getTxId());
                //add tx,status=END
                coreTxRepository.add(coreTxRepository.convertTxVO(tx), tx.getSignDatas(), CoreTxStatusEnum.END,blockHeight);
                //save process result
                coreTxRepository
                    .saveExecuteResult(tx.getTxId(), tx.getExecuteResult(), tx.getErrorCode(), tx.getErrorMsg());
                num++;
            } else {
                //check status
                if (CoreTxStatusEnum.formCode(coreTransactionPO.getStatus()) != CoreTxStatusEnum.END) {
                    CoreTxStatusEnum from = CoreTxStatusEnum.PERSISTED;
                    if(isfailOver){
                        from = CoreTxStatusEnum.formCode(coreTransactionPO.getStatus());
                    }
                    //update status
                    coreTxRepository.updateStatus(tx.getTxId(), from, CoreTxStatusEnum.END);
                    num++;
                } else {
                    log.error("[processEndForEach]tx status already END txId:{}", tx.getTxId());
                }
            }
        }
        return num == txs.size();
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
}
