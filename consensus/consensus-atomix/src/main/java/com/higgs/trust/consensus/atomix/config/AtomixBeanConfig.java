/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.core.DefaultCommitReplicateComposite;
import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveType;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.DefaultConsensusSnapshot;
import com.higgs.trust.consensus.core.IConsensusSnapshot;
import com.higgs.trust.consensus.core.filter.CompositeCommandFilter;
import io.atomix.cluster.discovery.NodeDiscoveryConfig;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.core.AtomixConfig;
import io.atomix.core.AtomixRegistry;
import io.atomix.core.profile.Profile;
import io.atomix.core.profile.ProfileConfig;
import io.atomix.core.utils.config.PolymorphicConfigMapper;
import io.atomix.core.utils.config.PolymorphicTypeMapper;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.config.PrimitiveConfig;
import io.atomix.primitive.partition.PartitionGroup;
import io.atomix.primitive.partition.PartitionGroupConfig;
import io.atomix.primitive.protocol.PrimitiveProtocol;
import io.atomix.primitive.protocol.PrimitiveProtocolConfig;
import io.atomix.utils.config.ConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Configuration @Slf4j public class AtomixBeanConfig {

    @Autowired @Bean public AbstractCommitReplicateComposite replicateComposite(CompositeCommandFilter filter) {
        return new DefaultCommitReplicateComposite(filter);
    }

    @Bean @ConditionalOnMissingBean(IConsensusSnapshot.class) public IConsensusSnapshot snapshot() {
        return new DefaultConsensusSnapshot();
    }

    @Bean public CommandPrimitiveType commandPrimitiveType(AbstractCommitReplicateComposite replicateComposite,
        IConsensusSnapshot snapshot) {
        return new CommandPrimitiveType(replicateComposite, snapshot);
    }

    @Bean public AtomixRegistry atomixRegistry() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (log.isDebugEnabled()) {
            log.debug("registry context class loader:{}", contextClassLoader);
        }

        return new SpringBeanAtomixRegistry(contextClassLoader, PartitionGroup.Type.class, PrimitiveType.class,
            PrimitiveProtocol.Type.class, Profile.Type.class, NodeDiscoveryProvider.Type.class);
    }

    @Bean public AtomixConfig atomixConfig(AtomixRegistry registry) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (log.isDebugEnabled()) {
            log.debug("config context class loader:{}", contextClassLoader);
        }
        ConfigMapper mapper = new PolymorphicConfigMapper(contextClassLoader, registry,
            new PolymorphicTypeMapper("type", PartitionGroupConfig.class, PartitionGroup.Type.class),
            new PolymorphicTypeMapper("type", PrimitiveConfig.class, PrimitiveType.class),
            new PolymorphicTypeMapper(null, PrimitiveConfig.class, PrimitiveType.class),
            new PolymorphicTypeMapper("type", PrimitiveProtocolConfig.class, PrimitiveProtocol.Type.class),
            new PolymorphicTypeMapper("type", ProfileConfig.class, Profile.Type.class),
            new PolymorphicTypeMapper("type", NodeDiscoveryConfig.class, NodeDiscoveryProvider.Type.class));
        return mapper.loadFiles(AtomixConfig.class, null, Arrays.asList("atomix", "defaults"));
    }
}
