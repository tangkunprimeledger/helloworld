/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.managment.master.ChangeMasterService;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Order(2) @Component @Slf4j public class PackageCommandFilter implements CommandFilter {
    @Autowired private NodeState nodeState;

    @Autowired private ChangeMasterService changeMasterService;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof PackageCommand) {
            PackageCommand command = (PackageCommand)commit.operation();
            Long term = command.getTerm();
            Long height = command.get().getHeight();
            String nodeName = command.getNodeName();
            if (!nodeState.isTermHeight(term, nodeName, height)) {
                log.warn("package command rejected");
                commit.close();
                return;
            }
            if (term == nodeState.getCurrentTerm()) {
                nodeState.resetEndHeight(height);
                changeMasterService.renewHeartbeatTimeout();
            }
        }
        chain.doFilter(commit);
    }
}
