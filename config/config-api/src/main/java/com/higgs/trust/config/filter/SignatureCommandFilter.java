/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.p2p.AbstractClusterInfo;
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

    @Autowired private AbstractClusterInfo clusterInfo;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof SignatureCommand) {
            clusterInfo.refreshIfNeed();
            SignatureCommand command = (SignatureCommand)commit.operation();
            String nodeName = command.getNodeName();
            String publicKey = clusterInfo.pubKeyForConsensus(nodeName);
            boolean verify =
                CryptoUtil.getProtocolCrypto().verify(command.getSignValue(), command.getSignature(), publicKey);
            if (log.isDebugEnabled()){
                log.debug("command sign verify result:{}", verify);
            }
            if (!verify) {
                log.warn("command sign verify failed, node:{}, pubkey:{}", nodeName, publicKey);
                commit.close();
                return;
            }
        }
        chain.doFilter(commit);
    }
}
