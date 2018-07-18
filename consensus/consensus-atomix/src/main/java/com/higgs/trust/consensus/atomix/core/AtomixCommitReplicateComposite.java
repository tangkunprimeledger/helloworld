package com.higgs.trust.consensus.atomix.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;

public class AtomixCommitReplicateComposite extends AbstractCommitReplicateComposite {
    @Override public ConsensusCommit commitAdapter(Object request) {
        return new AtomixCommitAdapter(request);
    }
}
