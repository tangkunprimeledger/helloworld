package com.higgs.trust.slave.core.service.pending;

import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.PendingTxRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
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
                transactionVO.setErrMsg("param invalid");
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }

            // pending transaction idempotent check
            if (pendingTxRepository.isExist(txId)) {
                log.warn("pending transaction idempotent, txId={}", txId);
                transactionVO.setErrMsg("transaction idempotent");
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                return;
            }

            // chained transaction idempotent check, cannot retry
            if (transactionRepository.isExist(txId)) {
                log.warn("transaction idempotent, txId={}", txId);
                transactionVO.setErrMsg("transaction idempotent");
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }

            // insert db
            pendingTxRepository.saveWithStatus(signedTransaction, PendingTxStatusEnum.INIT, null);
        });

        return transactionVOList;
    }

    @Override
    public void addPendingTransactions(List<SignedTransaction> transactions, PendingTxStatusEnum status, Long height) {
        if (CollectionUtils.isEmpty(transactions)) {
            log.error("no transaction to insert");
            return;
        }
        transactions.forEach(transaction->{
            pendingTxRepository.saveWithStatus(transaction, status, height);
        });
    }

    @Override public List<SignedTransaction> getPendingTransactions(int count) {
        return pendingTxRepository.getTransactionsByStatus(PendingTxStatusEnum.INIT.getCode(), count);
    }

    @Override public int packagePendingTransactions(List<SignedTransaction> signedTransactions, Long height) {
        return pendingTxRepository.batchUpdateStatus(signedTransactions, PendingTxStatusEnum.INIT, PendingTxStatusEnum.PACKAGED, height);
    }

    @Override public List<SignedTransaction> getPackagedTransactions(Long height) {
        return pendingTxRepository.getTransactionsByHeight(height);
    }
}
