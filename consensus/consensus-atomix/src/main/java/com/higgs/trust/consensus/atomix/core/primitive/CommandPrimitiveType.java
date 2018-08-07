/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.atomix.example.ExampleCommand;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.PrimitiveService;
import io.atomix.primitive.service.ServiceConfig;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Namespaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitiveType
    implements PrimitiveType<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    private AbstractCommitReplicateComposite replicateComposite;

    public CommandPrimitiveType() {
    }

    public CommandPrimitiveType(AbstractCommitReplicateComposite replicateComposite) {
        this.replicateComposite = replicateComposite;
    }

    @Override public CommandPrimitiveConfig newConfig() {
        return new CommandPrimitiveConfig(this);
    }

    @Override public CommandPrimitiveBuilder newBuilder(String primitiveName, CommandPrimitiveConfig config,
        PrimitiveManagementService managementService) {
        return new CommandPrimitiveBuilder(this, primitiveName, config, managementService);
    }

    @Override public PrimitiveService newService(ServiceConfig config) {
        return new CommandPrimitiveService(this, config, replicateComposite);
    }

    @Override public String name() {
        return CommandPrimitiveType.class.getSimpleName();
    }

    @Override public Namespace namespace() {
//        Set<Class<?>> classes = replicateComposite.registerCommit().keySet();
//        Class[] classArray = classes.toArray(new Class[classes.size()]);
        return Namespace.builder()
            .register(PrimitiveType.super.namespace())
//            .register(classArray)
            .register(ExampleCommand.class)
            .register(AbstractConsensusCommand.class)
            .build();
    }
}
