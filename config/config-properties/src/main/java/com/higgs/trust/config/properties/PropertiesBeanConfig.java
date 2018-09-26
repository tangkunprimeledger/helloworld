/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.properties;

import com.higgs.trust.consensus.core.DefaultConsensusSnapshot;
import com.higgs.trust.consensus.core.IConsensusSnapshot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/9/5
 */
@Configuration public class PropertiesBeanConfig {

    @Bean @ConditionalOnMissingBean(IConsensusSnapshot.class) public IConsensusSnapshot snapshot() {
        return new DefaultConsensusSnapshot();
    }

}
