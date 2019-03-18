/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.filter;

import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

public interface CommandFilterChain {

    void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit);
}
