/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.config.master.ChangeMasterService;
import com.higgs.trust.config.master.MasterHeartbeatService;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.config.node.command.TermCommand;
import com.higgs.trust.config.term.TermManager;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Order(2) @Component @Slf4j public class TermCommandFilter implements CommandFilter {
    @Autowired private NodeState nodeState;

    @Autowired private ChangeMasterService changeMasterService;

    @Autowired private MasterHeartbeatService masterHeartbeatService;

    @Autowired private TermManager termManager;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof TermCommand) {
            TermCommand command = (TermCommand)commit.operation();
            Long term = command.getTerm();
            Long[] height = command.getPackageHeight();

            //check height array
            if (!checkHeight(height)) {
                log.warn("package command height list is not continuous, height={}", height);
                commit.close();
                return;
            }

            String nodeName = command.getNodeName();
            if (!termManager.isTermHeight(term, nodeName, height[0])) {
                log.warn("package command rejected,current termInfo:{}", termManager.getTermInfo(term));
                commit.close();
                return;
            }
            if (term == nodeState.getCurrentTerm()) {
                termManager.resetEndHeight(height[height.length - 1]);
                changeMasterService.renewHeartbeatTimeout();
                if (nodeState.isMaster()) {
                    masterHeartbeatService.resetMasterHeartbeat();
                }
            }
        }
        chain.doFilter(commit);
    }

    private boolean checkHeight(Long[] height) {
        for (int i = 0; i < height.length - 1; i++) {
            if ((height[i] + 1) != height[i + 1]) {
                return false;
            }
        }
        return true;
    }
}
