package com.higgs.trust.slave.core.service.pending;

import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.managment.master.MasterPackageCache;
import com.higgs.trust.slave.core.repository.PendingTxRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
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
    @Autowired private PendingTxRepository pendingTxRepository;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private MasterPackageCache packageCache;

    /**
     * add pending transaction to db
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

            // params check
            BeanValidateResult validateResult = BeanValidator.validate(signedTransaction);
            if (!validateResult.isSuccess()) {
                log.error("transaction invalid. errMsg={}, txId={}", validateResult.getFirstMsg(), txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PARAM_INVALID.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PARAM_INVALID.getDesc());
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }

            // chained transaction idempotent check, cannot retry
            //TODO rocks db isExist method
            if (transactionRepository.isExist(txId)) {
                log.warn("transaction idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
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

            //check exist tx map and pending_tx_index
            //TODO rocks db isExist method
            if (packageCache.isExistInMap(txId) || pendingTxRepository.isExist(txId)) {
                log.warn("pending transaction idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                return;
            }

            // key and value all are txId
            packageCache.putExistMap(signedTransaction.getCoreTx().getTxId(), signedTransaction.getCoreTx().getTxId());
            //insert memory
            packageCache.appendDequeLast(signedTransaction);
        });

        // if all transaction received success, RespData will set data 'null'
        if (CollectionUtils.isEmpty(transactionVOList)) {
            return null;
        }
        return transactionVOList;
    }

    @Override public List<SignedTransaction> getPendingTransactions(int count) {
       return packageCache.getPendingTxQueue(count);
    }

    @Override public int packagePendingTransactions(List<SignedTransaction> signedTransactions, Long height) {
        return pendingTxRepository.batchUpdateStatus(signedTransactions, PendingTxStatusEnum.INIT, PendingTxStatusEnum.PACKAGED, height);
    }

    @Override public List<SignedTransaction> getPackagedTransactions(Long height) {
        return pendingTxRepository.getTransactionsByHeight(height);
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
