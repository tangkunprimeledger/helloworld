/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CompositeCommandFilter;

/**
 * @author suimi
 * @date 2018/8/14
 */
public class DefaultCommitReplicateComposite extends AbstractCommitReplicateComposite {
    public DefaultCommitReplicateComposite(CompositeCommandFilter filter) {
        super(filter);
    }

    @Override public ConsensusCommit<? extends AbstractConsensusCommand> commitAdapter(Object request) {
        return new DefaultCommitAdapter<>(request);
    }
}
