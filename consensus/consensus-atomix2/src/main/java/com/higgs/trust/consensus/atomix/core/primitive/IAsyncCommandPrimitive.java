/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.AsyncPrimitive;
import io.atomix.primitive.DistributedPrimitive;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
public interface IAsyncCommandPrimitive extends AsyncPrimitive{

    CompletableFuture<Void> submit(AbstractConsensusCommand command);

    @Override
    default ICommandPrimitive sync() {
        return sync(Duration.ofMillis(DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }

    @Override
    ICommandPrimitive sync(Duration operationTimeout);

}
