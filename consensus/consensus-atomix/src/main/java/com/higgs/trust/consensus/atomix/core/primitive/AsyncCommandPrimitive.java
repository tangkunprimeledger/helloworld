/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.AbstractAsyncPrimitive;
import io.atomix.primitive.PrimitiveRegistry;
import io.atomix.primitive.proxy.ProxyClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class AsyncCommandPrimitive extends AbstractAsyncPrimitive<AsyncCommandPrimitive, ICommandPrimitiveService>
    implements IAsyncCommandPrimitive {
    protected AsyncCommandPrimitive(ProxyClient<ICommandPrimitiveService> client, PrimitiveRegistry registry) {
        super(client, registry);
    }

    @Override public CompletableFuture<Void> submit(AbstractConsensusCommand command) {
        return getProxyClient().acceptBy(name(), service -> service.submit(command));
    }

    @Override public CommandPrimitive sync() {
        return null;
    }

    @Override public CommandPrimitive sync(Duration operationTimeout) {
        return null;
    }
}
