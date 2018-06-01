/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.filter;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.ConsensusCommit;

public interface CommandFilterChain {

    void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit);
}
