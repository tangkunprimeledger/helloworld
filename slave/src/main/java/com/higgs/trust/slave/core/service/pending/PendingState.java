package com.higgs.trust.slave.core.service.pending;

import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;

import java.util.List;

/**
 * @Description: hold all the processing transaction
 * @author: pengdi
 **/
public interface PendingState {
    /**
     * Add new transactions into pending transaction pool
     *
     * @param transactions
     * @return
     */
    List<TransactionVO> addPendingTransactions(List<SignedTransaction> transactions);

    /**
     * Get a specified number(count) of transactions
     *
     * @param count
     * @return
     */
    List<SignedTransaction> getPendingTransactions(int count);

    /**
     * Mark the transactions as packaged
     *
     * @param signedTransactions
     * @param height
     * @return
     */
    int packagePendingTransactions(List<SignedTransaction> signedTransactions, Long height);

    /**
     * Get packaged transactions based on block height
     *
     * @param height
     * @return
     */
    List<SignedTransaction> getPackagedTransactions(Long height);

    void addPendingTxsToQueueFirst(List<SignedTransaction> signedTransactions);
}
