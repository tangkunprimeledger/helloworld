package com.higgs.trust.consensus.bftsmart.started.adapter;

import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

public class SmartCommitAdapter<T extends AbstractConsensusCommand> implements ConsensusCommit<T> {

    private T command;

    public SmartCommitAdapter(Object object) {
        if (object instanceof AbstractConsensusCommand) {
            this.command = (T) object;
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

    @Override
    public boolean isClosed() {
        return false;
    }
}
