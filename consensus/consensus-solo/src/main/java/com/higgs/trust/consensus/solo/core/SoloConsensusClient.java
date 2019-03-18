package com.higgs.trust.consensus.solo.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author suimi
 * @date 2019/2/20
 */
@Service public class SoloConsensusClient implements ConsensusClient {

    @Autowired private AbstractCommitReplicateComposite commitReplicateComposite;

    @Override public <T> CompletableFuture<Void> submit(AbstractConsensusCommand<T> command) {
        Function function = commitReplicateComposite.registerCommit().get(command.getClass());
        return CompletableFuture.runAsync(() -> function.apply(command));
    }
}
