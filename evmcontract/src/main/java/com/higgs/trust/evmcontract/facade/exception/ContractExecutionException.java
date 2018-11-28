package com.higgs.trust.evmcontract.facade.exception;

/**
 * Thrown to indicate that an exception happens during the contract is
 * executed in the virtual machine.
 *
 * @author Chen Jiawei
 * @date 2018-11-16
 */
public class ContractExecutionException extends RuntimeException {
    private static final long serialVersionUID = 6661955926284899781L;

    public ContractExecutionException(String message) {
        super(message);
    }

    public ContractExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
