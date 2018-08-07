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

/**
 * @author suimi
 * @date 2018/7/6
 */
@Component public class CommandPrimitiveType
    implements PrimitiveType<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    @Autowired private AbstractCommitReplicateComposite replicateComposite;

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
        return "consensus-command";
    }

    @Override public Namespace namespace() {
        return Namespace.builder()
            .register(Namespaces.BASIC)
            .register(ServiceConfig.class)
            .register(ExampleCommand.class)
            .register(AbstractConsensusCommand.class)
            .build();
    }
}
