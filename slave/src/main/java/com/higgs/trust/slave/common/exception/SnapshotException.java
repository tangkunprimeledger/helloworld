package com.higgs.trust.slave.common.exception;

import com.higgs.trust.common.exception.ErrorInfo;

/**
 *snapshot exception
 *
 * @author lingchao
 * @create 2018年04月17日17:11
 */
public class SnapshotException extends SlaveException {

    public SnapshotException(SlaveException e) {
        super(e);
    }
    public SnapshotException(ErrorInfo code) {
        super(code);
    }

    public SnapshotException(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public SnapshotException(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public SnapshotException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }


}