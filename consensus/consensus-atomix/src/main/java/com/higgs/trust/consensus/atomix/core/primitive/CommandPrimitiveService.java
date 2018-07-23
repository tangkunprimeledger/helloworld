/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.AbstractPrimitiveService;
import io.atomix.primitive.service.BackupInput;
import io.atomix.primitive.service.BackupOutput;
import io.atomix.primitive.service.ServiceConfig;

/**
 * @author suimi
 * @date 2018/7/6
 */
public class CommandPrimitiveService extends AbstractPrimitiveService implements ICommandPrimitiveService {

    private Long index = 0L;

    protected CommandPrimitiveService(PrimitiveType primitiveType) {
        super(primitiveType);
    }

    public CommandPrimitiveService(ServiceConfig config) {
        super(CommandPrimitiveType.INSTANCE);
    }

    @Override public Void submit(AbstractConsensusCommand command) {
        return null;
    }

    @Override public void backup(BackupOutput output) {
        output.writeObject(index);
    }

    @Override public void restore(BackupInput input) {
        index = input.readObject();

    }
}
