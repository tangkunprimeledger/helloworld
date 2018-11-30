package com.higgs.trust.evmcontract.facade.compile;

import com.higgs.trust.evmcontract.solidity.Abi;

import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2018-11-29
 */
public class ContractInvocation {
    private Abi.Function function;

    public byte[] encodeInput(String methodSignature, Object... args) {
        Abi.Function function = Abi.Function.of(methodSignature);
        this.function = function;
        return function.encode(args);
    }

    public List<?> decodeResult(byte[] result) {
        return function == null ? null : function.decodeResult(result, false);
    }

    public byte[] getBytecodeForInvokeContract(String methodSignature, Object... args) {
        return encodeInput(methodSignature, args);
    }
}
