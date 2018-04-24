package com.higgs.trust.consensus.bft.adapter;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.bft.core.ConsensusCommand;
import io.atomix.copycat.Command;
import io.atomix.copycat.client.CopycatClient;

import java.util.concurrent.CompletableFuture;

public class CopycatClientAdapter implements ConsensusClient {

    private CopycatClient copycatClient;

    public CopycatClientAdapter(CopycatClient copycatClient) {
        this.copycatClient = copycatClient;
    }

    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        if (command instanceof Command) {
            return copycatClient.submit((Command<T>) command);
        }
        throw new RuntimeException("the command is not support");
    }
}
