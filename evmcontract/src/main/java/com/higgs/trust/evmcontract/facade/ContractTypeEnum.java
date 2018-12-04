package com.higgs.trust.evmcontract.facade;

/**
 * Type of contract. Especially, asset transfer between two account
 * is considered as a contract for simple processing.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public enum ContractTypeEnum {
    /**
     * Contract creation.
     */
    CONTRACT_CREATION,

    /**
     * Customer contract invocation.
     */
    CUSTOMER_CONTRACT_INVOCATION,

    /**
     * Customer contract invocation, for contract querying.
     */
    CUSTOMER_CONTRACT_QUERYING,

    /**
     * Precompiled contract invocation.
     */
    PRECOMPILED_CONTRACT_INVOCATION,

    /**
     * Asset transfer.
     */
    ASSET_TRANSFER
}
