package com.higgs.trust.slave.core.service.pending;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.master.MasterPackageCache;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.TxSubmitResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
@Service @Slf4j public class PendingStateImpl implements PendingState {

    @Autowired private MasterPackageCache packageCache;

    @Autowired private TransactionValidator transactionValidator;

    /**
     * add pending transaction to db
     *
     * @param transactions
     * @return transaction list which check failed
     */
    @Override public List<TransactionVO> addPendingTransactions(List<SignedTransaction> transactions) {

        if (CollectionUtils.isEmpty(transactions)) {
            log.error("received transaction list is empty");
            return null;
        }

        List<TransactionVO> transactionVOList = new ArrayList<>();
        transactions.forEach(signedTransaction -> {
            TransactionVO transactionVO = new TransactionVO();
            String txId = signedTransaction.getCoreTx().getTxId();
            transactionVO.setTxId(txId);
            //verify params for transaction
            try {
                transactionValidator.verify(signedTransaction);
            } catch (SlaveException e){
                transactionVO.setErrCode(e.getCode().getCode());
                transactionVO.setErrMsg(e.getCode().getDescription());
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }
            //limit queue size
            if (packageCache.getPendingTxQueueSize() > Constant.MAX_PENDING_TX_QUEUE_SIZE) {
                log.warn("pending transaction queue size is too large , txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_QUEUE_SIZE_TOO_LARGE.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_QUEUE_SIZE_TOO_LARGE.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                return;
            }

            try {
                //insert memory
                boolean result = packageCache.appendDequeLast(signedTransaction);
                if (!result) {
                    log.warn("pending transaction map idempotent, txId={}", txId);
                    transactionVO.setErrCode(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getCode());
                    transactionVO.setErrMsg(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getDesc());
                    transactionVO.setRetry(true);
                    transactionVOList.add(transactionVO);
                }
            } catch (Throwable e) {
                log.error("transaction insert into memory exception. txId={}, ", txId, e);
            }
        });

        // if all transaction received success, RespData will set data 'null'
        if (CollectionUtils.isEmpty(transactionVOList)) {
            return null;
        }
        return transactionVOList;
    }

    @Override public Object[] getPendingTransactions(int count) {
        return packageCache.getPendingTxQueue(count);
    }

    @Override public void addPendingTxsToQueueFirst(List<SignedTransaction> signedTransactions) {
        for (SignedTransaction signedTx : signedTransactions) {
            try {
                packageCache.appendDequeFirst(signedTx);
            } catch (Exception e) {
                log.error("add transaction to pendingTxQueue exception. ", e);
            }
        }
    }
}
