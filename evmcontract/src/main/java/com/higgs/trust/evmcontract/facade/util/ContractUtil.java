package com.higgs.trust.evmcontract.facade.util;

import java.math.BigInteger;

/**
 * Tool class for contract-related logic.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class ContractUtil {
    /**
     * Defined to avoid an instance being created from outside.
     */
    private ContractUtil() {
    }

    public static BigInteger toBigInteger(byte[] v) {
        return new BigInteger(1, v);
    }

    public static boolean notEqual(BigInteger v1, BigInteger v2) {
        return v1.compareTo(v2) != 0;
    }

    public static boolean notEqual(byte[] v1, byte[] v2) {
        return notEqual(toBigInteger(v1), toBigInteger(v2));
    }

    public static boolean moreThan(BigInteger v1, BigInteger v2) {
        return v1.compareTo(v2) > 0;
    }

    public static boolean moreThan(byte[] v1, byte[] v2) {
        return moreThan(toBigInteger(v1), toBigInteger(v2));
    }
}
