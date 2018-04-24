/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import lombok.Getter;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@Getter @Setter public class ClusterHeightCmd extends IdConsensusCommand<Integer> {

    private static final long serialVersionUID = -2067709119627092336L;

    public ClusterHeightCmd(String requestId, String nodeName, Integer value) {
        super(requestId, nodeName, value);
    }
}
