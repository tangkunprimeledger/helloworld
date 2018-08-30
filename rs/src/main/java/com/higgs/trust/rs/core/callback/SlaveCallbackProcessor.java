package com.higgs.trust.rs.core.callback;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.DistributeCallbackNotifyService;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.api.enums.RedisMegGroupEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.repository.CoreTxProcessRepository;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
@Component
@Slf4j
public class SlaveCallbackProcessor implements SlaveCallbackHandler, InitializingBean {
    @Autowired
    private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired
    private CoreTxRepository coreTxRepository;
    @Autowired
    private CoreTxProcessRepository coreTxProcessRepository;
    @Autowired
    private RsCoreCallbackProcessor rsCoreCallbackProcessor;
    @Autowired
    private VoteRuleRepository voteRuleRepository;
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private DistributeCallbackNotifyService distributeCallbackNotifyService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!rsConfig.isBatchCallback()) {
            slaveCallbackRegistor.registCallbackHandler(this);
        }
    }

    @Override
    public void onPersisted(RespData<CoreTransaction> respData, List<SignInfo> signInfos, BlockHeader blockHeader) {
        CoreTransaction tx = respData.getData();
        CallbackTypeEnum callbackType = getCallbackType(tx);
        if (callbackType == CallbackTypeEnum.SELF && !sendBySelf(tx.getSender())) {
            log.debug("[onPersisted]only call self");
            return;
        }

        //not send by myself
        if (!sendBySelf(tx.getSender())) {
            //add tx,status=PERSISTED
            createCoreTx(tx, signInfos, respData, blockHeader.getHeight(), CoreTxStatusEnum.PERSISTED);
            //callback custom rs
            rsCoreCallbackProcessor.onPersisted(respData, blockHeader);
            return;
        }

        //check core_tx record
        if (!coreTxRepository.isExist(tx.getTxId())) {
            log.warn("onPersisted]call back self but core_tx is not exist txId:{}", tx.getTxId());
            createCoreTx(tx, signInfos, respData, blockHeader.getHeight(), CoreTxStatusEnum.PERSISTED);
            //callback custom rs
            rsCoreCallbackProcessor.onPersisted(respData, blockHeader);
            //同步通知
            distributeCallbackNotifyService.notifySyncResult(tx.getTxId(), respData, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
            return;
        }

        //for self
        //save process result
        coreTxRepository.saveExecuteResultAndHeight(tx.getTxId(), respData.isSuccess() ? CoreTxResultEnum.SUCCESS : CoreTxResultEnum.FAIL, respData.getRespCode(), respData.getMsg(), blockHeader.getHeight());
        //update status
        coreTxProcessRepository.updateStatus(tx.getTxId(), CoreTxStatusEnum.WAIT, CoreTxStatusEnum.PERSISTED);
        //callback custom rs
        rsCoreCallbackProcessor.onPersisted(respData, blockHeader);
        //同步通知
        distributeCallbackNotifyService.notifySyncResult(tx.getTxId(), respData, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
    }

    @Override
    public void onClusterPersisted(RespData<CoreTransaction> respData, List<SignInfo> signInfos, BlockHeader blockHeader) {
        CoreTransaction tx = respData.getData();
        CallbackTypeEnum callbackType = getCallbackType(tx);
        if (callbackType == CallbackTypeEnum.SELF && !sendBySelf(tx.getSender())) {
            log.debug("[onClusterPersisted]only call self");
            return;
        }

        //update status to END
        try {
            coreTxProcessRepository.updateStatus(tx.getTxId(), CoreTxStatusEnum.PERSISTED, CoreTxStatusEnum.END);
        } catch (RsCoreException e) {
            //status is END no need to deal
            if (RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED != e.getCode()) {
                throw e;
            }
        }

        //callback custom rs
        rsCoreCallbackProcessor.onEnd(respData, blockHeader);
        //同步通知
        distributeCallbackNotifyService.notifySyncResult(tx.getTxId(), respData, RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
    }

    @Override
    public void onFailover(RespData<CoreTransaction> respData, List<SignInfo> signInfos, BlockHeader blockHeader) {
        CoreTransaction tx = respData.getData();
        CallbackTypeEnum callbackType = getCallbackType(tx);
        if (callbackType == CallbackTypeEnum.SELF && !sendBySelf(tx.getSender())) {
            log.debug("[onFailover]only call self");
            return;
        }
        //create core_transaction
        createCoreTx(tx, signInfos, respData, blockHeader.getHeight(), CoreTxStatusEnum.END);
        //callback custom rs
        rsCoreCallbackProcessor.onFailover(respData, blockHeader);
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
     * create coreTx and coreTxProcess
     *
     * @param tx
     * @param signInfos
     * @param respData
     * @param blockHeight
     */
    private void createCoreTx(CoreTransaction tx, List<SignInfo> signInfos, RespData respData, Long blockHeight, CoreTxStatusEnum coreTxStatusEnum) {
        coreTxRepository.add(tx, signInfos, respData.isSuccess() ? CoreTxResultEnum.SUCCESS : CoreTxResultEnum.FAIL, respData.getRespCode(), respData.getMsg(), blockHeight);
        // END coreTxProcess  will be delete by task, so no need to insert it.
        if (coreTxStatusEnum != CoreTxStatusEnum.END) {
            coreTxProcessRepository.add(tx.getTxId(), coreTxStatusEnum);
        }
    }
}
