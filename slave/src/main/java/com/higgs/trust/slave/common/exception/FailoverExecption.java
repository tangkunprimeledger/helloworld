/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.common.exception;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class FailoverExecption extends SlaveException {

    public FailoverExecption(SlaveException e) {
        super(e);
    }

    public FailoverExecption(SlaveErrorEnum code) {
        super(code);
    }

    public FailoverExecption(SlaveErrorEnum code, String errorMessage) {
        super(code, errorMessage);
    }

    public FailoverExecption(SlaveErrorEnum code, Throwable cause) {
        super(code, cause);
    }

    public FailoverExecption(SlaveErrorEnum code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
