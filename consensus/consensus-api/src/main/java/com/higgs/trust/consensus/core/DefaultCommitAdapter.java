/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

/**
 * @author suimi
 * @date 2018/8/14
 */
public class DefaultCommitAdapter<T extends AbstractConsensusCommand> implements ConsensusCommit<T> {
    private T command;
    private boolean isClosed;

    public DefaultCommitAdapter(Object object) {
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
