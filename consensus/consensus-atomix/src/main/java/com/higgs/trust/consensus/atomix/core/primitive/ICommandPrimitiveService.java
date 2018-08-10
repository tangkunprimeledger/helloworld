/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.core.primitive;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.primitive.operation.Command;

/**
 * @author suimi
 * @date 2018/7/6
 */
public interface ICommandPrimitiveService {

    @Command void submit(AbstractConsensusCommand command);
}
