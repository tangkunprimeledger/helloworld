/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

/**
 * @author suimi
 * @date 2018/8/14
 */
public class AtomixCommitReplicateComposite extends AbstractCommitReplicateComposite {
    @Override public ConsensusCommit<? extends AbstractConsensusCommand> commitAdapter(Object request) {
        return new AtomixCommitAdapter<>(request);
    }
}
