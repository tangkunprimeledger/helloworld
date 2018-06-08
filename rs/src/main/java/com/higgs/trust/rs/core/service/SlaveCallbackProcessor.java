package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.asynctosync.HashBlockingMap;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
@Component @Slf4j public class SlaveCallbackProcessor implements SlaveCallbackHandler, InitializingBean {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private SlaveCallbackRegistor slaveCallbackRegistor;
    @Autowired private CoreTransactionDao coreTransactionDao;
    @Autowired private RsCoreCallbackHandler rsCoreCallbackHandler;
    @Autowired private HashBlockingMap<RespData> persistedResultMap;
    @Autowired private HashBlockingMap<RespData> clusterPersistedResultMap;
    @Autowired private NodeState nodeState;

    /**
     * @param sender
     * @return
     */
    private boolean sendBySelf(String sender) {
        if (StringUtils.equals(nodeState.getNodeName(), sender)) {
            return true;
        }
        return false;
    }

    @Override public void afterPropertiesSet() throws Exception {
        slaveCallbackRegistor.registCallbackHandler(this);
    }

    @Override public void onValidated(CoreTransaction coreTx) {
        //not send by myself, don't execute
        if (!sendBySelf(coreTx.getSender())) {
            return;
        }
        CoreTransactionPO po = coreTransactionDao.queryByTxId(coreTx.getTxId(), false);
        if (po == null) {
            //TODO:liuyu 需要考虑failover
            log.warn("[onValidated]query core transaction is null by txId:{}", coreTx.getTxId());
            return;
        }
        //        txRequired.execute(new TransactionCallbackWithoutResult() {
        //            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
        //                int r = coreTransactionDao.updateStatus(coreTx.getTxId(), CoreTxStatusEnum.WAIT.getCode(),
        //                    CoreTxStatusEnum.VALIDATED.getCode());
        //                if (r != 1) {
        //                    log.error("[onValidated] update tx status is fail,txId:{}, from:{},to:{}",coreTx.getTxId(), CoreTxStatusEnum.WAIT,
        //                        CoreTxStatusEnum.VALIDATED);
        //                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
        //                }
        //            }
        //        });
    }

    @Override public void onPersisted(RespData<CoreTransaction> respData) {
        CoreTransaction tx = respData.getData();

        //not send by myself, don't execute
        if (!sendBySelf(tx.getSender())) {
            return;
        }

        CoreTransactionPO po = coreTransactionDao.queryByTxId(tx.getTxId(), false);
        if (po == null) {
            //TODO:liuyu 需要考虑failover
            log.warn("[onPersisted]query core transaction is null by txId:{}", tx.getTxId());
            return;
        }

        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTxStatusEnum fromStatus = CoreTxStatusEnum.WAIT;
                //TODO:liuyu 临时方案,由于failover时，没有执行onValidated
                if (StringUtils.equals(po.getStatus(), CoreTxStatusEnum.WAIT.getCode())) {
                    fromStatus = CoreTxStatusEnum.WAIT;
                    log.info("[onPersisted]current status is not VALIDATED by txId:{}", tx.getTxId());
                    log.info("[onPersisted]change current status to WAIT by txId:{}", tx.getTxId());
                }
                int r = coreTransactionDao
                    .updateStatus(tx.getTxId(), fromStatus.getCode(), CoreTxStatusEnum.PERSISTED.getCode());
                if (r != 1) {
                    log.error("[onValidated] update tx status is fail,txId:{},from:{},to:{}", tx.getTxId(), fromStatus,
                        CoreTxStatusEnum.PERSISTED);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
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

    @Override public void onClusterPersisted(RespData<CoreTransaction> respData) {
        CoreTransaction tx = respData.getData();

        //not send by myself, don't execute
        if (!sendBySelf(tx.getSender())) {
            return;
        }

        CoreTransactionPO po = coreTransactionDao.queryByTxId(tx.getTxId(), false);
        if (po == null) {
            //TODO:liuyu 需要考虑failover
            log.warn("[onClusterPersisted]query core transaction is null by txId:{}", tx.getTxId());
            return;
        }

        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                int r = coreTransactionDao
                    .updateStatus(tx.getTxId(), CoreTxStatusEnum.PERSISTED.getCode(), CoreTxStatusEnum.END.getCode());
                if (r != 1) {
                    log.error("[onClusterPersisted] update tx status is fail,txId:{},from:{},to:{}", tx.getTxId(),
                        CoreTxStatusEnum.PERSISTED, CoreTxStatusEnum.END);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
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
}
