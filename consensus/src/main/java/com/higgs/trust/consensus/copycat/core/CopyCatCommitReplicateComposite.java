package com.higgs.trust.consensus.copycat.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.copycat.adapter.CopycatCommitAdapter;
import com.higgs.trust.consensus.core.ConsensusCommit;

public class CopyCatCommitReplicateComposite extends AbstractCommitReplicateComposite {
    @Override public ConsensusCommit commitAdapter(Object request) {
        return new CopycatCommitAdapter(request);
    }
}
