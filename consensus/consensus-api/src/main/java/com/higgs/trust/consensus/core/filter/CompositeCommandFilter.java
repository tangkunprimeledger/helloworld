/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.filter;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.ConsensusCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Component public class CompositeCommandFilter {
    
    @Autowired private List<CommandFilter> filters;

    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit) {
        new VirtualCommandFilterChain(filters).doFilter(commit);
    }
}
