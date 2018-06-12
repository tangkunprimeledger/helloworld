package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.asynctosync.HashBlockingMap;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
@Component @Slf4j public class SlaveCallbackProcessor implements SlaveCallbackHandler, InitializingBean {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired private CoreTxRepository coreTxRepository;
    @Autowired private RsCoreCallbackHandler rsCoreCallbackHandler;
    @Autowired private VoteRuleRepository voteRuleRepository;
    @Autowired private HashBlockingMap<RespData> persistedResultMap;
    @Autowired private HashBlockingMap<RespData> clusterPersistedResultMap;
    @Autowired private RsConfig rsConfig;

    /**
     * system is fail over status
     */
    private boolean isFailOver;

    @Override public void afterPropertiesSet() throws Exception {
        slaveCallbackRegistor.registCallbackHandler(this);
    }

    @Override public void onValidated(CoreTransaction coreTx) {
    }

    @Override public void onPersisted(RespData<CoreTransaction> respData,List<SignInfo> signInfos) {
        CoreTransaction tx = respData.getData();
        CallbackTypeEnum callbackType = getCallbackType(tx);
        if(callbackType == CallbackTypeEnum.SELF){
            //not send by myself, don't execute
            if (!sendBySelf(tx.getSender())) {
                log.info("[onPersisted]only call self");
                return;
            }
        }else{
            log.info("[onPersisted]call all");
            //not send by myself
            if (!sendBySelf(tx.getSender())) {
                CoreTransactionPO po = coreTxRepository.queryByTxId(tx.getTxId(), false);
                if (po == null) {
                    //add tx,status=END
                    coreTxRepository.add(tx,signInfos,CoreTxStatusEnum.END);
                    //callback custom rs
                    if(isFailOver){
                        rsCoreCallbackHandler.onFailOver(respData);
                    }else{
                        rsCoreCallbackHandler.onPersisted(respData);
                    }
                }
                return;
            }
        }
        //for self
        //process fail over
        if(isFailOver){
            log.info("[onPersisted]for isFailOver");
            CoreTransactionPO po = coreTxRepository.queryByTxId(tx.getTxId(), false);
            if (po == null) {
                //add tx,status=END
                coreTxRepository.add(tx, signInfos, CoreTxStatusEnum.END);
                rsCoreCallbackHandler.onFailOver(respData);
            }
            return;
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                //update status
                coreTxRepository.updateStatus(tx.getTxId(), CoreTxStatusEnum.WAIT, CoreTxStatusEnum.PERSISTED);
                //callback custom rs
                rsCoreCallbackHandler.onPersisted(respData);
            }
        });
        //同步通知
        try {
            persistedResultMap.put(tx.getTxId(), respData);
        } catch (Throwable e) {
            log.warn("sync notify rs resp data failed", e);
        }
    }

    @Override public void onClusterPersisted(RespData<CoreTransaction> respData,List<SignInfo> signInfos) {
        CoreTransaction tx = respData.getData();
        CallbackTypeEnum callbackType = getCallbackType(tx);
        if(callbackType == CallbackTypeEnum.SELF){
            //not send by myself, don't execute
            if (!sendBySelf(tx.getSender())) {
                log.info("[onClusterPersisted]only call self");
                return;
            }
        }else{
            log.info("[onClusterPersisted]call all");
            //not send by myself
            if (!sendBySelf(tx.getSender())) {
                CoreTransactionPO po = coreTxRepository.queryByTxId(tx.getTxId(), false);
                if (po == null) {
                    //add tx,status=END
                    coreTxRepository.add(tx,signInfos,CoreTxStatusEnum.END);
                }
                //callback custom rs
                if(!isFailOver){
                    rsCoreCallbackHandler.onEnd(respData);
                }
                return;
            }
        }
        //for self
        //process fail over
        if(isFailOver){
            return;
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                //update status
                coreTxRepository.updateStatus(tx.getTxId(), CoreTxStatusEnum.PERSISTED, CoreTxStatusEnum.END);
                //callback custom rs
                rsCoreCallbackHandler.onEnd(respData);
            }
        });
        //同步通知
        try {
            clusterPersistedResultMap.put(tx.getTxId(), respData);
        } catch (Throwable e) {
            log.warn("sync notify rs resp data failed", e);
        }
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
        log.info("[getCallbackType]callbackType:{}", callbackType);
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
