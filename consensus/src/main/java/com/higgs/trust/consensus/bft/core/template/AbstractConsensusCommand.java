package com.higgs.trust.consensus.bft.core.template;

import com.higgs.trust.consensus.bft.core.ConsensusCommand;
import io.atomix.copycat.Command;

/**
 * @author cwy
 */
public abstract class AbstractConsensusCommand<T> implements ConsensusCommand<T>, Command<T> {
    private static final long serialVersionUID = 1L;
    private T value;
    private Long tranceId;
    public AbstractConsensusCommand(T value) {
        this.value = value;
    }
    @Override
    public T get() {
        return this.value;
    }

    public Long getTranceId() {
        return tranceId;
    }

    public void setTranceId(Long tranceId) {
        this.tranceId = tranceId;
    }

    @Override
    public CompactionMode compaction() {
        return CompactionMode.FULL;
    }

}
