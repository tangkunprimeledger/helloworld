package com.higgs.trust.slave.core.service.transaction;

import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.TransactionData;

import java.util.Map;

/**
 * @Description:
 * @author: pengdi
 **/
public interface TransactionExecutor {

    /**
     * persist all transactions,return validate results and persistedDatas
     *
     * @param transactionData
     * @param rsPubKeyMap
     * @return
     */
    TransactionReceipt process(TransactionData transactionData, Map<String, String> rsPubKeyMap);
}
