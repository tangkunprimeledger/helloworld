/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.service.AbstractPrimitiveService;
import io.atomix.primitive.service.Commit;
import io.atomix.primitive.service.ServiceExecutor;
import io.atomix.storage.buffer.BufferInput;
import io.atomix.storage.buffer.BufferOutput;
import io.atomix.utils.serializer.KryoNamespace;
import io.atomix.utils.serializer.KryoNamespaces;
import io.atomix.utils.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j public class CommandPrimitiveService extends AbstractPrimitiveService {

    private AbstractCommitReplicateComposite replicateComposite;

    private ConsensusSnapshot snapshot;

    private final Serializer SERIALIZER;

    private CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation;

    public CommandPrimitiveService(AbstractCommitReplicateComposite replicateComposite,
                                   ConsensusSnapshot snapshot, CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation) {
        this.replicateComposite = replicateComposite;
        this.snapshot = snapshot;
        this.commandPrimitiveSubmitOperation = commandPrimitiveSubmitOperation;
        SERIALIZER = Serializer.using(KryoNamespace.builder()
                .register(KryoNamespaces.BASIC)
                .register(commandPrimitiveSubmitOperation.NAMESPACE)
                .register(ConsensusSnapshot.class)
                .setRegistrationRequired(false)
                .build());
    }

    @Override
    protected void configure(ServiceExecutor executor) {
        executor.register(commandPrimitiveSubmitOperation, SERIALIZER::decode, this::submit);
    }

    @Override
    public void backup(BufferOutput<?> output) {
        output.writeObject(snapshot.getSnapshot(),SERIALIZER::encode);
    }

    @Override
    public void restore(BufferInput<?> input) {
        String snapshotStr = input.readObject(SERIALIZER::decode);
        snapshot.installSnapshot(snapshotStr);
    }

    protected void submit(Commit<AbstractConsensusCommand> commit) {
        log.debug("Received Command");
        AbstractConsensusCommand command = commit.value();
        Function function = replicateComposite.registerCommit().get(command.getClass());
        function.apply(command);
    }
}
