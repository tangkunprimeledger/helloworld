/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.AbstractAsyncPrimitive;
import io.atomix.primitive.PrimitiveRegistry;
import io.atomix.primitive.SyncPrimitive;
import io.atomix.primitive.proxy.ProxyClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitive extends AbstractAsyncPrimitive<ICommandPrimitive, ICommandPrimitiveService>
    implements ICommandPrimitive {
    protected CommandPrimitive(ProxyClient<ICommandPrimitiveService> client, PrimitiveRegistry registry) {
        super(client, registry);
    }

    @Override public CompletableFuture<Void> submit(AbstractConsensusCommand command) {
        return getProxyClient().applyBy(name(), service -> service.submit(command));
    }

    @Override public SyncPrimitive sync() {
        return null;
    }

    @Override public SyncPrimitive sync(Duration operationTimeout) {
        return null;
    }
}
