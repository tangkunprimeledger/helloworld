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
    Object[] getPendingTransactions(int count);

    void addPendingTxsToQueueFirst(List<SignedTransaction> signedTransactions);
}
