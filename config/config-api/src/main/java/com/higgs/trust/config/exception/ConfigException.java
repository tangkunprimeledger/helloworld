/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.exception;

import com.higgs.trust.common.exception.ErrorInfo;
import com.higgs.trust.common.exception.TrustException;

/**
 * @author suimi
 * @date 2018/6/12
 */
public class ConfigException extends TrustException {
    private static final long serialVersionUID = -3350277997782674259L;

    public ConfigException(TrustException e) {
        super(e);
    }

    public ConfigException(ErrorInfo code) {
        super(code);
    }

    public ConfigException(ErrorInfo code, String errorMessage) {
        super(code, errorMessage);
    }

    public ConfigException(ErrorInfo code, Throwable cause) {
        super(code, cause);
    }

    public ConfigException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(code, errorMessage, cause);
    }
}
