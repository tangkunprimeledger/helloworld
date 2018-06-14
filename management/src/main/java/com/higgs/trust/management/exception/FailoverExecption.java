/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.management.exception;

import com.higgs.trust.common.exception.ErrorInfo;
import com.higgs.trust.slave.common.exception.SlaveException;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class FailoverExecption extends SlaveException {

    public FailoverExecption(SlaveException e) {
        super(e);
    }

    public FailoverExecption(ErrorInfo code) {
        super(code);
    }

    public FailoverExecption(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public FailoverExecption(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public FailoverExecption(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
