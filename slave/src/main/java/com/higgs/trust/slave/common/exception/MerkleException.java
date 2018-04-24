package com.higgs.trust.slave.common.exception;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

/**
 * @author WangQuanzhou
 * @desc MerkleException
 * @date 2018/4/18 11:43
 */
public class MerkleException extends SlaveException {
    public MerkleException(SlaveException e) {
        super(e);
    }

    public MerkleException(SlaveErrorEnum code) {
        super(code);
    }

    public MerkleException(SlaveErrorEnum code, String errorMessage) {
        super(code, errorMessage);
    }

    public MerkleException(SlaveErrorEnum code, Throwable cause) {
        super(code, cause);
    }

    public MerkleException(SlaveErrorEnum code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}