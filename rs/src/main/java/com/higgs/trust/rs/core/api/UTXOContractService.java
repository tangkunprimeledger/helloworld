package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.model.bo.CoreTransaction;

/**
 * UTXO contract process
 *
 * @author lingchao
 * @create 2018年06月29日1:04
 */
public interface UTXOContractService {
    /**
     * process UTXO contract
     *
     * @param coreTransaction
     * @return
     */
    boolean process(CoreTransaction coreTransaction);
}
