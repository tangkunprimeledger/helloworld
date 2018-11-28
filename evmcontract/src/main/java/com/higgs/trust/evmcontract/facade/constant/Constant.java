package com.higgs.trust.evmcontract.facade.constant;

import java.math.BigInteger;

/**
 * To the project, some Solidity concepts are meaningless. To make processing
 * simple, some parameters should be set as constants.
 *
 * @author Chen Jiawei
 * @date 2018-11-19
 */
public class Constant {
    /**
     * Size limitation of transaction payload.
     */
    public static final int TRANSACTION_DATA_SIZE_LIMIT = 1024 * 1024;

    /**
     * Gas price. Zero means that no fee is required for contract execution.
     */
    public static final byte[] TRANSACTION_GAS_PRICE = new byte[0];

    /**
     * Gas limitation. It should be small enough in the condition being
     * enough to execute normal contract.
     */
    public static final byte[] TRANSACTION_GAS_LIMIT = BigInteger.valueOf(100_000_000L).toByteArray();

    /**
     * Consensus protocol is not of PoW, the difficulty of generating a
     * block is considered as zero.
     */
    public static final byte[] BLOCK_DIFFICULTY = new byte[0];

    /**
     * Just reserved for Solidity program.
     */
    public static final byte[] BLOCK_GAS_LIMIT = BigInteger.valueOf(200_000_000L).toByteArray();

    /**
     * Defined to avoid an instance being created from outside.
     */
    private Constant() {
    }
}
