package com.higgs.trust.slave.core.service.transaction;

import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.TransactionData;

/**
 * @Description:
 * @author: pengdi
 **/
public interface TransactionExecutor {
    /**
     * validate all transactions,return validate results and validatedDatas
     *
     * @param transactionData
     * @return
     */
    TransactionReceipt validate(TransactionData transactionData);

    /**
     * persist all transactions,return validate results and persistedDatas
     *
     * @param transactionData
     * @return
     */
    TransactionReceipt persist(TransactionData transactionData);
}
