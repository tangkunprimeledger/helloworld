package com.higgs.trust.consensus.bftsmart.started.adapter;

import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;

public class SmartCommitAdapter<T extends AbstractConsensusCommand> implements ConsensusCommit<T> {

    private T command;

    public SmartCommitAdapter(T object) {
        if (object instanceof AbstractConsensusCommand) {
            this.command = object;
        } else {
            throw new RuntimeException("the commit is not support!");
        }
    }

    @Override
    public T operation() {
        return command;
    }

    @Override
    public void close() {
        //do nothing
    }
}
