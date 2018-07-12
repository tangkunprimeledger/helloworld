/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(ConsensusSnapshot.class)
public class DefaultConsensusSnapshot implements ConsensusSnapshot {

    @Override
    public String getSnapshot() {
        return "N/A";
    }

    @Override
    public void installSnapshot(String snapshot) {
        return;
    }
}
