package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import com.higgs.trust.consensus.bftsmartcustom.started.custom.adapter.SmartCommitAdapter;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.filter.CompositeCommandFilter;
import org.springframework.stereotype.Component;

/**
 * @author: zhouyafeng
 * @create: 2018/06/05 17:47
 * @description:
 */
@Component public class SmartCommitReplicateComposite extends AbstractCommitReplicateComposite {

    public SmartCommitReplicateComposite(CompositeCommandFilter filter) {
        super(filter);
    }

    @Override public ConsensusCommit commitAdapter(Object request) {
        return new SmartCommitAdapter(request);
    }
}