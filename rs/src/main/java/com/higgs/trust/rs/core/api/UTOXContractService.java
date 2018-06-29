package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.model.bo.action.UTXOAction;

/**
 * UTXO contract process
 *
 * @author lingchao
 * @create 2018年06月29日1:04
 */
public interface UTOXContractService {
    /**
     * process UTXO contract
     *
     * @param action
     * @param contractAddress
     * @return
     */
    boolean process(UTXOAction action, String contractAddress);
}
