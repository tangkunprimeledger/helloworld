/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.exception;

import com.higgs.trust.common.exception.ErrorInfo;
import com.higgs.trust.common.exception.TrustException;

/**
 * @author suimi
 * @date 2018/6/12
 */
public class ConsensusException extends TrustException {
    private static final long serialVersionUID = -3350277997782674259L;

    public ConsensusException(TrustException e) {
        super(e);
    }

    public ConsensusException(ErrorInfo code) {
        super(code);
    }

    public ConsensusException(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public ConsensusException(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public ConsensusException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
