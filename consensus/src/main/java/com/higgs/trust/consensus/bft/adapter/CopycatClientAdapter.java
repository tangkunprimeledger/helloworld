package com.higgs.trust.consensus.bft.adapter;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.bft.core.ConsensusCommand;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.consensus.common.TraceUtils;
import io.atomix.copycat.Command;
import io.atomix.copycat.client.CopycatClient;

import java.util.concurrent.CompletableFuture;

/**
 * @author cwy
 */
public class CopycatClientAdapter implements ConsensusClient {

    private CopycatClient copycatClient;

    public CopycatClientAdapter(CopycatClient copycatClient) {
        this.copycatClient = copycatClient;
    }

    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        if (command instanceof Command) {
            if(command instanceof AbstractConsensusCommand){
                ((AbstractConsensusCommand)command).setTranceId(TraceUtils.getTraceId());
            }
            return copycatClient.submit((Command<T>) command);
        }
        throw new RuntimeException("the command is not support");
    }
}
