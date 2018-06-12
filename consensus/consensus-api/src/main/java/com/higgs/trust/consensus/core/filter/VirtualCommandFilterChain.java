/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.filter;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.ConsensusCommit;

import java.util.List;

/**
 * @author suimi
 * @date 2018/6/1
 */
public class VirtualCommandFilterChain implements CommandFilterChain {

    private List<CommandFilter> filters;

    private int currentPosition = 0;

    public VirtualCommandFilterChain(List<CommandFilter> filters) {
        this.filters = filters;
    }

    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit) {
        if (this.currentPosition != this.filters.size()) {
            this.currentPosition++;
            CommandFilter nextFilter = this.filters.get(this.currentPosition - 1);
            nextFilter.doFilter(commit, this);
        }
    }

}
