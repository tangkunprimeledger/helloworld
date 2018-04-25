package com.higgs.trust.slave.common.exception;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

/**
 * @author duhongming
 * @date 2018/04/25
 */
public class ContractException extends SlaveException {
    public ContractException(SlaveException e) {
        super(e);
    }

    public ContractException(SlaveErrorEnum code) {
        super(code);
    }

    public ContractException(SlaveErrorEnum code, String errorMessage) {
        super(code, errorMessage);
    }

    public ContractException(SlaveErrorEnum code, Throwable cause) {
        super(code, cause);
    }

    public ContractException(SlaveErrorEnum code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
