package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.slave.api.SlaveCallbackHandler;
import com.higgs.trust.slave.api.SlaveCallbackRegistor;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import lombok.extern.slf4j.Slf4j;
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
    @Autowired private TxCallbackRegistor txCallbackRegistor;

    @Override public void afterPropertiesSet() throws Exception {
        slaveCallbackRegistor.registCallbackHandler(this);
    }

    @Override public void onValidated(String txId) {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                int r = coreTransactionDao
                    .updateStatus(txId, CoreTxStatusEnum.WAIT.getCode(), CoreTxStatusEnum.VALIDATED.getCode());
                if (r != 1) {
                    log.error("[onValidated] update tx status is fail from:{},to:{}", CoreTxStatusEnum.WAIT, CoreTxStatusEnum.VALIDATED);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            }
        });
    }

    @Override public void onPersisted(TransactionReceipt transactionReceipt, CoreTransaction tx) {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                int r = coreTransactionDao
                    .updateStatus(tx.getTxId(), CoreTxStatusEnum.VALIDATED.getCode(), CoreTxStatusEnum.PERSISTED.getCode());
                if (r != 1) {
                    log.error("[onValidated] update tx status is fail from:{},to:{}", CoreTxStatusEnum.VALIDATED, CoreTxStatusEnum.PERSISTED);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
                TxCallbackHandler txCallbackHandler = txCallbackRegistor.getCoreTxCallback();
                if(txCallbackHandler == null){
                    log.error("[onValidated]call back handler is not register");
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
                }
                CoreTransactionPO po = coreTransactionDao.queryByTxId(tx.getTxId(),false);
                RespData respData = new RespData();
                if(!transactionReceipt.isResult()) {
                    respData.setCode(transactionReceipt.getErrorCode());
                }
                respData.setData(tx);
                //callback custom rs
                txCallbackHandler.onPersisted(BizTypeEnum.fromCode(po.getBizType()),respData);
            }
        });
    }

    @Override public void onClusterPersisted(TransactionReceipt transactionReceipt, CoreTransaction tx) {

    }
}
