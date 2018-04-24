package com.higgs.trust.consensus.bft.core.template;

import com.higgs.trust.consensus.bft.core.ConsensusCommand;
import io.atomix.copycat.Command;

public abstract class AbstractConsensusCommand<T> implements ConsensusCommand<T>, Command<T> {
    private static final long serialVersionUID = 1L;
    private T value;

    public AbstractConsensusCommand(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    @Override
    public CompactionMode compaction() {
        return CompactionMode.FULL;
    }

}
