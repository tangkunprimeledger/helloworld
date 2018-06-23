package com.higgs.trust.consensus.core;

import com.higgs.trust.consensus.core.command.ConsensusCommand;

import java.util.concurrent.CompletableFuture;

/**
 * @author cwy
 */
public interface ConsensusClient {
    /**
     * @param command command context
     * @param <T> generic type of load
     * @return
     */
    <T> CompletableFuture<T> submit(ConsensusCommand<T> command);

}
