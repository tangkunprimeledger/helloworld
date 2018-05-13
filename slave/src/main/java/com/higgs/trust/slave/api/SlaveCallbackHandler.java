package com.higgs.trust.slave.api;

import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
public interface SlaveCallbackHandler {
    /**
     * on tx validated
     * @param txId
     */
    void onValidated(String txId);

    /**
     * on tx persisted
     * @param transactionReceipt
     * @param tx
     */
    void onPersisted(TransactionReceipt transactionReceipt,CoreTransaction tx);

    /**
     * when the cluster persisted of tx
     *
     * @param transactionReceipt
     * @param tx
     */
    void onClusterPersisted(TransactionReceipt transactionReceipt,CoreTransaction tx);
}
