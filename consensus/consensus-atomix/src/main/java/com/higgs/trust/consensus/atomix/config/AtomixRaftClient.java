/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.ConsensusCommand;
import io.atomix.protocols.raft.RaftServer;
import io.atomix.protocols.raft.protocol.RaftServerProtocol;

import java.util.concurrent.CompletableFuture;

/**
 * @author suimi
 * @date 2018/7/2
 */
public class AtomixRaftClient implements ConsensusClient {

    NodeProperties nodeProperties;

    public void init() {
    }

    @Override public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
//        atomix.getCommunicationService().broadcast(command.getClass().getSimpleName(), command);
        return null;
    }
}
