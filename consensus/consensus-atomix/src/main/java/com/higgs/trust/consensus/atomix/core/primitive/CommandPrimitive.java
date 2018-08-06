/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.*;
import io.atomix.primitive.proxy.ProxyClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitive extends Synchronous<IAsyncCommandPrimitive> implements ICommandPrimitive {

    public CommandPrimitive(IAsyncCommandPrimitive primitive) {
        super(primitive);
    }

    @Override public void submit(AbstractConsensusCommand command) {

    }

    @Override public IAsyncCommandPrimitive async() {
        return null;
    }
}
