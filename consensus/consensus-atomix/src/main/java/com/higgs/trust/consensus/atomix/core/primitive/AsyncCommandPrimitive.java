/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.AbstractAsyncPrimitive;
import io.atomix.primitive.DistributedPrimitive;
import io.atomix.primitive.PrimitiveRegistry;
import io.atomix.primitive.proxy.ProxyClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j public class AsyncCommandPrimitive
    extends AbstractAsyncPrimitive<AsyncCommandPrimitive, ICommandPrimitiveService> implements IAsyncCommandPrimitive {
    protected AsyncCommandPrimitive(ProxyClient<ICommandPrimitiveService> client, PrimitiveRegistry registry) {
        super(client, registry);
    }

    @Override public CompletableFuture<Void> submit(AbstractConsensusCommand command) {
        if (log.isDebugEnabled()) {
            log.debug("async submit");
        }
        return getProxyClient().acceptBy(name(), service -> service.submit(command));
    }

    @Override public ICommandPrimitive sync() {
        return sync(Duration.ofMillis(DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }

    @Override public ICommandPrimitive sync(Duration operationTimeout) {
        if (log.isDebugEnabled()) {
            log.debug("async sync");
        }
        return new CommandPrimitive(this, operationTimeout.toMillis());
    }
}
