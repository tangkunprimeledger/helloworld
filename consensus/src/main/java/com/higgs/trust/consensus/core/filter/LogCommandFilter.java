/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.filter;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.ConsensusCommit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Component @Slf4j public class LogCommandFilter implements CommandFilter {
    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        AbstractConsensusCommand operation = commit.operation();
        if (log.isDebugEnabled()) {
            log.debug("received command:{}", operation);
        }
        chain.doFilter(commit);
    }
}
