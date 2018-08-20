/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.PrimitiveService;
import io.atomix.primitive.service.ServiceConfig;
import io.atomix.utils.serializer.Namespace;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j public class CommandPrimitiveType
    implements PrimitiveType<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    private AbstractCommitReplicateComposite replicateComposite;

    private ConsensusSnapshot snapshot;

    public CommandPrimitiveType(AbstractCommitReplicateComposite replicateComposite, ConsensusSnapshot snapshot) {
        this.replicateComposite = replicateComposite;
        this.snapshot = snapshot;
    }

    @Override public CommandPrimitiveConfig newConfig() {
        return new CommandPrimitiveConfig(this);
    }

    @Override public CommandPrimitiveBuilder newBuilder(String primitiveName, CommandPrimitiveConfig config,
        PrimitiveManagementService managementService) {
        return new CommandPrimitiveBuilder(this, primitiveName, config, managementService);
    }

    @Override public PrimitiveService newService(ServiceConfig config) {
        return new CommandPrimitiveService(this, replicateComposite, snapshot);
    }

    @Override public String name() {
        return CommandPrimitiveType.class.getSimpleName();
    }

    @Override public Namespace namespace() {
        Set<Class<?>> commandClasses = replicateComposite.registerCommit().keySet();
        Set<Class<?>> classes = new HashSet<>(commandClasses);
        List<Class<?>> classList = new ArrayList<>();
        classes.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(clazz->classList.add(clazz));
        return Namespace.builder()
            .setRegistrationRequired(false)
            .register(PrimitiveType.super.namespace())
            .register(AbstractConsensusCommand.class)
            .register(classList.toArray(new Class[classList.size()]))
            .build();
    }
}
