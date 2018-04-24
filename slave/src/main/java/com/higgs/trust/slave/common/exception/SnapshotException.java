package com.higgs.trust.slave.common.exception;


import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

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
    public SnapshotException(SlaveErrorEnum code) {
        super(code);
    }

    public SnapshotException(SlaveErrorEnum code, String errorMessage) {
        super(code, errorMessage);
    }

    public SnapshotException(SlaveErrorEnum code, Throwable cause) {
        super(code, cause);
    }

    public SnapshotException(SlaveErrorEnum code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }


}