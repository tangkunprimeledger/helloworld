/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@NoArgsConstructor @Getter @Setter public class ClusterHeightCmd extends ValidCommand<Integer> {

    private static final long serialVersionUID = -2067709119627092336L;

    private String requestId;

    public ClusterHeightCmd(String requestId, Integer value) {
        super(value);
        this.requestId = requestId;
    }

    @Override public String messageDigest() {
        return requestId;
    }
}
