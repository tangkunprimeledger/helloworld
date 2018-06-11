package com.higgs.trust.consensus.copycat.core;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j public class CopycatStateMachine extends StateMachine implements Snapshottable {

    private AbstractCommitReplicateComposite commitReplicate;

    private ConsensusSnapshot snapshot;

    public CopycatStateMachine(AbstractCommitReplicateComposite commitReplicate, ConsensusSnapshot snapshot) {
        this.commitReplicate = commitReplicate;
        this.snapshot = snapshot;
    }

    @Override protected void configure(StateMachineExecutor executor) {
        commitReplicate.registerCommit().forEach((key, value) -> register(key, value));
    }

    private void register(Class type, Function function) {
        executor.register(type, function);
    }

    @Override public void snapshot(SnapshotWriter writer) {
        String snapshot = this.snapshot.getSnapshot();
        writer.writeUTF8(snapshot).flush();
    }

    @Override public void install(SnapshotReader reader) {
        String snapshot = reader.readUTF8();
        this.snapshot.installSnapshot(snapshot);
    }
}
