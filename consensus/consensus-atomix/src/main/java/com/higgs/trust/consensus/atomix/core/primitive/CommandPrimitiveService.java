/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.service.AbstractPrimitiveService;
import io.atomix.primitive.service.BackupInput;
import io.atomix.primitive.service.BackupOutput;
import io.atomix.primitive.service.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j public class CommandPrimitiveService extends AbstractPrimitiveService implements ICommandPrimitiveService {

    private AbstractCommitReplicateComposite replicateComposite;

    private ConsensusSnapshot snapshot;

    public CommandPrimitiveService(CommandPrimitiveType type, ServiceConfig config,
        AbstractCommitReplicateComposite replicateComposite, ConsensusSnapshot snapshot) {
        super(type, ICommandPrimitive.class);
        this.replicateComposite = replicateComposite;
        this.snapshot = snapshot;
    }

    @Override public void submit(AbstractConsensusCommand command) {
        Function function = replicateComposite.registerCommit().get(command.getClass());
        function.apply(command);
    }

    @Override public void backup(BackupOutput output) {
        output.writeObject(snapshot.getSnapshot());
    }

    @Override public void restore(BackupInput input) {
        String snapshotStr = input.readObject();
        snapshot.installSnapshot(snapshotStr);
    }
}
