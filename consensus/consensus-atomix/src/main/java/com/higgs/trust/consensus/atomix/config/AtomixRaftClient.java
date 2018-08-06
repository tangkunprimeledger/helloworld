/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/2
 */
public class AtomixRaftClient implements ConsensusClient {

    @Override public <T> CompletableFuture<?> submit(AbstractConsensusCommand<T> command) {
        return null;
    }
}
