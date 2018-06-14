package com.higgs.trust.slave.common.exception;

import com.higgs.trust.common.exception.ErrorInfo;

/**
 * @author duhongming
 * @date 2018/04/25
 */
public class ContractException extends SlaveException {
    public ContractException(SlaveException e) {
        super(e);
    }

    public ContractException(ErrorInfo code) {
        super(code);
    }

    public ContractException(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public ContractException(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public ContractException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
