/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.PrimitiveService;
import io.atomix.primitive.service.ServiceConfig;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitiveType
    implements PrimitiveType<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    protected static final CommandPrimitiveType INSTANCE = new CommandPrimitiveType();

    public static CommandPrimitiveType instance() {
        return INSTANCE;
    }

    @Override public CommandPrimitiveConfig newConfig() {
        return new CommandPrimitiveConfig();
    }

    @Override public CommandPrimitiveBuilder newBuilder(String primitiveName, CommandPrimitiveConfig config,
        PrimitiveManagementService managementService) {
        return new CommandPrimitiveBuilder(CommandPrimitiveType.instance(), primitiveName, config, managementService);
    }

    @Override public PrimitiveService newService(ServiceConfig config) {
        return new CommandPrimitiveService(config);
    }

    @Override public String name() {
        return "consensus-command";
    }
}
