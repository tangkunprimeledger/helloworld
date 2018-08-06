package com.higgs.trust.consensus.core;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

import java.util.concurrent.CompletableFuture;

/**
 * @author cwy
 */
public interface ConsensusClient {
    /**
     * @param command command context
     * @param <T>     generic type of load
     * @return
     */
    <T> CompletableFuture<?> submit(AbstractConsensusCommand<T> command);

}
