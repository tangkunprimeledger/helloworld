package com.higgs.trust.slave.api;

import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-07-27
 */
public interface SlaveBatchCallbackHandler {
    /**
     * on tx persisted
     *
     * @param txs
     * @param txReceipts
     * @param blockHeader
     */
    void onPersisted(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,BlockHeader blockHeader);

    /**
     * when the cluster persisted of tx
     *
     * @param txs
     * @param txReceipts
     * @param blockHeader
     */
    void onClusterPersisted(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,BlockHeader blockHeader);

    /**
     * on failover
     *
     * @param txs
     * @param txReceipts
     * @param blockHeader
     */
    void onFailover(List<SignedTransaction> txs, List<TransactionReceipt> txReceipts,BlockHeader blockHeader);
}
