/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.AsyncPrimitive;

import java.util.concurrent.CompletableFuture;

public interface ICommandPrimitive extends AsyncPrimitive {

    CompletableFuture<Void> submit(AbstractConsensusCommand command);
}
