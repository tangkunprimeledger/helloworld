/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.PrimitiveBuilder;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/8/6
 */
@Slf4j
public class CommandPrimitiveBuilder
    extends PrimitiveBuilder<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {
    public CommandPrimitiveBuilder(PrimitiveType type, String name, CommandPrimitiveConfig config,
        PrimitiveManagementService managementService) {
        super(type, name, config, managementService);
        if (log.isDebugEnabled()) {
            log.debug("new builder");
        }
    }

    @Override public CompletableFuture<ICommandPrimitive> buildAsync() {
        if (log.isDebugEnabled()) {
            log.debug("build async");
        }
        return newProxy(ICommandPrimitiveService.class, new ServiceConfig())
            .thenCompose(proxy -> new AsyncCommandPrimitive(proxy, managementService.getPrimitiveRegistry()).connect())
            .thenApply(AsyncCommandPrimitive::sync);
    }

}
