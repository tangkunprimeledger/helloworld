package com.higgs.trust.evmcontract.facade.exception;

/**
 * Thrown to indicate that the contract context contains an illegal
 * or inappropriate status given by contract builder.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class ContractContextException extends IllegalArgumentException {
    private static final long serialVersionUID = 7373404340319963398L;

    public ContractContextException(String message) {
        super(message);
    }
}
