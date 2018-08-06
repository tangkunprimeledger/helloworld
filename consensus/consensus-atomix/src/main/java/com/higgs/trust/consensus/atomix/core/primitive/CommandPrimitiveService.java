/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.AbstractPrimitiveService;
import io.atomix.primitive.service.BackupInput;
import io.atomix.primitive.service.BackupOutput;
import io.atomix.primitive.service.ServiceConfig;

import java.util.function.Function;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitiveService extends AbstractPrimitiveService implements ICommandPrimitiveService {

    private Long index = 0L;

    AbstractCommitReplicateComposite replicateComposite;

    public CommandPrimitiveService(ServiceConfig config) {
        super(CommandPrimitiveType.INSTANCE);
    }

    @Override public void submit(AbstractConsensusCommand command) {
        Function function = replicateComposite.registerCommit().get(command.getClass());
        function.apply(command);
    }

    @Override public void backup(BackupOutput output) {
        output.writeObject(index);
    }

    @Override public void restore(BackupInput input) {
        index = input.readObject();

    }
}
