/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.core.AtomixCommitReplicateComposite;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import io.atomix.core.profile.Profile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Configuration @Slf4j public class AtomixConfig {

    @Bean public AbstractCommitReplicateComposite replicateComposite() {
        return new AtomixCommitReplicateComposite();
    }

//    @Bean public AtomixRaftProfile profile(AtomixRaftProperties properties) {
//        return new AtomixRaftProfile(properties);
//    }
}
