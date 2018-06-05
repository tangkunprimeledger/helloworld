package com.higgs.trust.consensus.core;

public interface ConsensusCommit<T> {
    T operation();

    void close();

    boolean isClosed();
}
