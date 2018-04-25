package com.higgs.trust.contract;

import java.io.IOException;

public class SmartContractException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SmartContractException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartContractException(String message) {
        super(message);
    }
}
