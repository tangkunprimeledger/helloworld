/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

public class DefaultConsensusSnapshot implements IConsensusSnapshot {

    @Override public byte[] getSnapshot() {
        return "N/A".getBytes();
    }

    @Override public void installSnapshot(byte[] snapshot) {
        return;
    }
}
