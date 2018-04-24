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
    public List<TransactionVO> addPendingTransactions(List<SignedTransaction> transactions);

    /**
     * Add new transactions into pending transaction pool
     *
     * @param transactions
     * @param status
     * @param height
     * @return
     */
    public void addPendingTransactions(List<SignedTransaction> transactions, PendingTxStatusEnum status, Long height);

    /**
     * Get a specified number(count) of transactions
     *
     * @param count
     * @return
     */
    public List<SignedTransaction> getPendingTransactions(int count);

    /**
     * Mark the transactions as packaged
     *
     * @param signedTransactions
     * @param height
     * @return
     */
    public int packagePendingTransactions(List<SignedTransaction> signedTransactions, Long height);

    /**
     * Get packaged transactions based on block height
     *
     * @param height
     * @return
     */
    public List<SignedTransaction> getPackagedTransactions(Long height);
}
