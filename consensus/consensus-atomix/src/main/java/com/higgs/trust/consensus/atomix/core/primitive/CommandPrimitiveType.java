/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.DistributedPrimitiveBuilder;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.config.PrimitiveConfig;
import io.atomix.primitive.service.PrimitiveService;
import io.atomix.primitive.service.ServiceConfig;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitiveType implements PrimitiveType {

    protected static final CommandPrimitiveType INSTANCE = new CommandPrimitiveType();

    @Override public PrimitiveConfig newConfig() {
        return null;
    }

    @Override public DistributedPrimitiveBuilder newBuilder(String primitiveName, PrimitiveConfig config,
        PrimitiveManagementService managementService) {
        return null;
    }

    @Override public PrimitiveService newService(ServiceConfig config) {
        return new CommandPrimitiveService(config);
    }

    @Override public String name() {
        return "consensus-command";
    }
}
