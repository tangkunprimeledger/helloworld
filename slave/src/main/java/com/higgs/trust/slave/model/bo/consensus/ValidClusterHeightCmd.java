/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class ValidClusterHeightCmd extends IdValidCommand<Long> {

    private static final long serialVersionUID = -7652400642865085127L;

    public ValidClusterHeightCmd(String id, Long height) {
        super(id, height);
    }

    @Override public String messageDigest() {
        return getRequestId() + get();
    }
}
