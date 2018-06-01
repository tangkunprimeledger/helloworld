package com.higgs.trust.consensus.core.command;

import io.atomix.copycat.Command;

/**
 * @author cwy
 */
public abstract class AbstractConsensusCommand<T> implements ConsensusCommand<T>, Command<T> {
    private static final long serialVersionUID = 1L;
    private T value;
    private Long traceId;
    public AbstractConsensusCommand(T value) {
        this.value = value;
    }
    @Override
    public T get() {
        return this.value;
    }

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    @Override
    public CompactionMode compaction() {
        return CompactionMode.QUORUM;
    }

}
