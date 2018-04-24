package com.higgs.trust.consensus.bft.core;

import java.util.concurrent.CompletableFuture;

public interface ConsensusClient {
    <T> CompletableFuture<T> submit(ConsensusCommand<T> command);
}
