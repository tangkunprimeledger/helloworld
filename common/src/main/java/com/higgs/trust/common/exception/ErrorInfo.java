/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.common.exception;

/**
 * @author suimi
 * @date 2018/6/12
 */
public interface ErrorInfo {
    /**
     * @return Returns the code.
     */
    String getCode();

    /**
     * @return Returns the description.
     */
    String getDescription();

    /**
     * @return
     */
    boolean isNeedRetry();
}
