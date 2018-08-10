/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.SyncPrimitive;

public interface ICommandPrimitive extends SyncPrimitive {

    void submit(AbstractConsensusCommand command);

    @Override IAsyncCommandPrimitive async();
}
