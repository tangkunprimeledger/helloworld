/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

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
