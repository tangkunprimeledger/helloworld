package com.higgs.trust.consensus.copycat.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;

import java.util.function.Function;

public class CopycateStateMachine extends StateMachine {

    private AbstractCommitReplicateComposite commitReplicate;

    public CopycateStateMachine(AbstractCommitReplicateComposite commitReplicate) {
        this.commitReplicate = commitReplicate;
    }

    @Override protected void configure(StateMachineExecutor executor) {
        commitReplicate.registerCommit().forEach((key, value) -> register(key, value));
    }

    private void register(Class type, Function function) {
        executor.register(type, function);
    }

}
