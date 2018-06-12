/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
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
@Order(1) @Component @Slf4j public class SignatureCommandFilter implements CommandFilter {

    @Autowired private ClusterInfo clusterInfo;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof SignatureCommand) {
            SignatureCommand command = (SignatureCommand)commit.operation();
            boolean verify = SignUtils
                .verify(command.getSignValue(), command.getSignature(), clusterInfo.pubKey(command.getNodeName()));
            log.debug("command sign verify:{}", verify);
            if (!verify) {
                log.warn("command sign verify failed.");
                commit.close();
                return;
            }
        }
        chain.doFilter(commit);
    }
}
