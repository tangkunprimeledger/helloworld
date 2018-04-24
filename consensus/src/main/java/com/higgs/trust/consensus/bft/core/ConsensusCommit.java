package com.higgs.trust.consensus.bft.core;

public interface ConsensusCommit<T> {
    T operation();

    void close();
}
