/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.impl.AbstractAsyncPrimitive;
import io.atomix.primitive.proxy.PrimitiveProxy;
import io.atomix.utils.serializer.KryoNamespace;
import io.atomix.utils.serializer.KryoNamespaces;
import io.atomix.utils.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j public class AsyncCommandPrimitive
    extends AbstractAsyncPrimitive implements IAsyncCommandPrimitive {

    private final Serializer SERIALIZER;

    private CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation;

    protected AsyncCommandPrimitive(PrimitiveProxy proxy, CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation) {
        super(proxy);
        this.commandPrimitiveSubmitOperation = commandPrimitiveSubmitOperation;
        SERIALIZER = Serializer.using(KryoNamespace.builder()
                .register(KryoNamespaces.BASIC)
                .register(commandPrimitiveSubmitOperation.NAMESPACE)
                .setRegistrationRequired(false)
                .build());
    }

    @Override public CompletableFuture<Void> submit(AbstractConsensusCommand command) {
        if (log.isDebugEnabled()) {
            log.debug("async submit");
        }
        return proxy.invoke(commandPrimitiveSubmitOperation, SERIALIZER::encode, command);
    }

    @Override public ICommandPrimitive sync(Duration operationTimeout) {
        if (log.isDebugEnabled()) {
            log.debug("async sync");
        }
        return new CommandPrimitive(this, operationTimeout.toMillis());
    }

    @Override
    public PrimitiveType primitiveType() {
        return null;
    }
}
