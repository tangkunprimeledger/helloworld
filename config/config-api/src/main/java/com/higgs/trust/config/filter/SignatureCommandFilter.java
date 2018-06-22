/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Order(1) @Component @Slf4j public class SignatureCommandFilter implements CommandFilter {

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private NodeState nodeState;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            log.warn("current node state:{}", nodeState.getState());
            throw new ConfigException(ConfigError.CONFIG_NODE_STATE_INVALID);
        }
        if (commit.operation() instanceof SignatureCommand) {
            SignatureCommand command = (SignatureCommand)commit.operation();
            String nodeName = command.getNodeName();
            String publicKey = clusterInfo.pubKey(nodeName);
            boolean verify = SignUtils.verify(command.getSignValue(), command.getSignature(), publicKey);
            log.debug("command sign verify:{}", verify);
            if (!verify) {
                log.warn("command sign verify failed, node:{}, pubkey:{}", nodeName, publicKey);
                commit.close();
                return;
            }
        }
        chain.doFilter(commit);
    }
}
