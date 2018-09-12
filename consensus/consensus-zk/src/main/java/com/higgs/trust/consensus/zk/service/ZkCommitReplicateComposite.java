package com.higgs.trust.consensus.zk.service;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.zk.adapter.ZkCommitAdapter;
import org.springframework.stereotype.Component;

/**
 * @author: zhouyafeng
 * @create: 2018/09/07 10:29
 * @description:
 */
@Component
public class ZkCommitReplicateComposite extends AbstractCommitReplicateComposite {
    @Override public ConsensusCommit commitAdapter(Object request) {
        return new ZkCommitAdapter(request);
    }
}