package com.higgs.trust.consensus.copycat.adapter;

import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusCommand;
import com.higgs.trust.consensus.core.AbstractConsensusCommand;
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

    /**
     * @param command command context
     * @param <T>
     * @return
     */
    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        if (command instanceof Command) {
            if(command instanceof AbstractConsensusCommand){
                ((AbstractConsensusCommand)command).setTraceId(TraceUtils.getTraceId());
            }
            return copycatClient.submit((Command<T>) command);
        }
        throw new RuntimeException("the command is not support");
    }
}
