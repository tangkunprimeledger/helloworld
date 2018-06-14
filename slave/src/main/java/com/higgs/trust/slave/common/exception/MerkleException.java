package com.higgs.trust.slave.common.exception;

import com.higgs.trust.common.exception.ErrorInfo;
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

    public MerkleException(ErrorInfo code) {
        super(code);
    }

    public MerkleException(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public MerkleException(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public MerkleException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}