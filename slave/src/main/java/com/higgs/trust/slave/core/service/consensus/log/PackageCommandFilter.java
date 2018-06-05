/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Component public class PackageCommandFilter implements CommandFilter {
    @Autowired private NodeState nodeState;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof PackageCommand) {
            PackageCommand command = (PackageCommand)commit.operation();
            Long term = command.get().getTerm();
            Long height = command.get().getHeight();
            String nodeName = command.getNodeName();
            long currentTerm = nodeState.getCurrentTerm();
            nodeState.getTerms().stream().filter(t -> t.getTerm() == currentTerm).collect(Collectors.toList());
            if (term > currentTerm) {
                commit.close();
                return;
            }
        }
        chain.doFilter(commit);
    }
}
