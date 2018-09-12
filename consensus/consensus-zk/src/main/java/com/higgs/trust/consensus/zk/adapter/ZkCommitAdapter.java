package com.higgs.trust.consensus.zk.adapter;

import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

public class ZkCommitAdapter<T extends AbstractConsensusCommand> implements ConsensusCommit<T> {

    private T command;
    private boolean isClosed;

    public ZkCommitAdapter(Object object) {
        if (object instanceof AbstractConsensusCommand) {
            this.command = (T)object;
        } else {
            throw new RuntimeException("the commit is not support!");
        }
    }

    @Override public T operation() {
        return command;
    }

    @Override public void close() {
        this.isClosed = true;
    }

    @Override public boolean isClosed() {
        return this.isClosed;
    }
}
