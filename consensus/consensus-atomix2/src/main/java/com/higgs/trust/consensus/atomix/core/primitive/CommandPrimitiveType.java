/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.service.PrimitiveService;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author suimi
 * @date 2018/7/6
 */
@Slf4j
public class CommandPrimitiveType
        implements PrimitiveType<CommandPrimitiveBuilder, CommandPrimitiveConfig, ICommandPrimitive> {

    private static final String NAME = "COMMAND_PRIMITIVE";

    private AbstractCommitReplicateComposite replicateComposite;

    private ConsensusSnapshot snapshot;

    private CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation;

    public CommandPrimitiveType(AbstractCommitReplicateComposite replicateComposite,
                                ConsensusSnapshot snapshot, CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation) {
        this.replicateComposite = replicateComposite;
        this.snapshot = snapshot;
        this.commandPrimitiveSubmitOperation = commandPrimitiveSubmitOperation;
    }

    @Override
    public String id() {
        return NAME;
    }

    @Override
    public Supplier<PrimitiveService> serviceFactory() {
        return () -> new CommandPrimitiveService(replicateComposite, snapshot, commandPrimitiveSubmitOperation);
    }

    @Override
    public CommandPrimitiveBuilder newPrimitiveBuilder(String name, PrimitiveManagementService managementService) {
        return newPrimitiveBuilder(name, new CommandPrimitiveConfig(this), managementService);
    }

    @Override
    public CommandPrimitiveBuilder newPrimitiveBuilder(String name, CommandPrimitiveConfig config, PrimitiveManagementService managementService) {
        return new CommandPrimitiveBuilder(this, name, config, managementService, commandPrimitiveSubmitOperation);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id())
                .toString();
    }
}
