/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.slave.model.bo.BlockHeader;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class ValidHeaderCmd extends IdValidCommand<Boolean> {

    private BlockHeader header;

    public ValidHeaderCmd(BlockHeader header, Boolean valid) {
        super(header.getBlockHash(), valid);
        this.header = header;
    }

    @Override public String messageDigest() {
        return header.getBlockHash() + this.get();
    }
}
