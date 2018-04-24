/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import lombok.Getter;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@Getter @Setter public class ClusterHeightCmd extends IdConsensusCommand<Integer> {

    public ClusterHeightCmd(String requestId, String nodeName, Integer value) {
        super(requestId, nodeName, value);
    }
}
