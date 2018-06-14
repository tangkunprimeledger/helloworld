package com.higgs.trust.consensus.copycat.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j public class CopycatStateMachine extends StateMachine {

    private AbstractCommitReplicateComposite commitReplicate;

    public CopycatStateMachine(AbstractCommitReplicateComposite commitReplicate) {
        this.commitReplicate = commitReplicate;
    }

    @Override protected void configure(StateMachineExecutor executor) {
        commitReplicate.registerCommit().forEach((key, value) -> register(key, value));
    }

    private void register(Class type, Function function) {
        executor.register(type, function);
    }
}
