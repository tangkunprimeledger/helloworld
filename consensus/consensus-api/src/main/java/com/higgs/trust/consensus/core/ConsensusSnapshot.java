/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

public interface ConsensusSnapshot {

    String getSnapshot();

    void installSnapshot(String snapshot);
}
