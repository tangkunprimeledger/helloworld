package com.higgs.trust.consensus.solo.config;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.DefaultCommitReplicateComposite;
import com.higgs.trust.consensus.core.filter.CompositeCommandFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2019/2/20
 */
@Configuration public class SoloBeanConfig {

    @Autowired @Bean public AbstractCommitReplicateComposite replicateComposite(CompositeCommandFilter filter) {
        return new DefaultCommitReplicateComposite(filter);
    }

}
