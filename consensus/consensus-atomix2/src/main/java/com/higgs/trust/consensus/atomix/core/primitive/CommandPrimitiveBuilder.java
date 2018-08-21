/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import io.atomix.primitive.DistributedPrimitiveBuilder;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.protocol.PrimitiveProtocol;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/8/6
 */
@Slf4j
public class CommandPrimitiveBuilder
    extends DistributedPrimitiveBuilder<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    private CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation;

    public CommandPrimitiveBuilder(PrimitiveType type, String name, CommandPrimitiveConfig config,
                                   PrimitiveManagementService managementService, CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation) {
        super(type, name, config, managementService);
        this.commandPrimitiveSubmitOperation = commandPrimitiveSubmitOperation;
        if (log.isDebugEnabled()) {
            log.debug("new builder");
        }
    }

    @Override public CompletableFuture<ICommandPrimitive> buildAsync() {
        PrimitiveProtocol protocol = protocol();
        if (log.isDebugEnabled()) {
            log.debug("build async");
        }
        return managementService.getPrimitiveRegistry().createPrimitive(name(), primitiveType())
                .thenCompose(info -> managementService.getPartitionService()
                .getPartitionGroup(protocol)
                .getPartition(name())
                .getPrimitiveClient()
                .newProxy(name(), primitiveType(), protocol)
                .connect()
                .thenApply(proxy -> new AsyncCommandPrimitive(proxy, commandPrimitiveSubmitOperation).sync()));
    }

    @Override public CommandPrimitiveBuilder withProtocol(PrimitiveProtocol protocol) {
        return super.withProtocol(protocol);
    }
}
