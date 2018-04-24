/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.Getter;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class ValidClusterHeightCmd extends IdValidCommand<Long> {

    public ValidClusterHeightCmd(String id, Long height) {
        super(id, height);
    }

    @Override public String messageDigest() {
        return getRequestId() + get();
    }
}
