/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

public interface IConsensusSnapshot {

    byte[] getSnapshot();

    void installSnapshot(byte[] snapshot);
}
