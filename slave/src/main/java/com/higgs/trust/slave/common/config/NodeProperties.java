package com.higgs.trust.slave.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component @ConfigurationProperties(prefix = "higgs.trust") @Getter @Setter public class NodeProperties {

    /**
     * default node name
     */
    private static final String DEFAULT_NODE_NAME = "DefaultNode";
    /**
     * the name of current node
     */
    private String nodeName = DEFAULT_NODE_NAME;

    /**
     * the name of master
     */
    private String masterName = DEFAULT_NODE_NAME;

    /**
     * the dir of consensus
     */
    private String consensusDir = "/tmp/higgs/trust/consensus";

    /**
     * keeping milliseconds of the consensus result
     */
    private long consensusKeepTime = 1 * 60 * 1000;

    /**
     * the milliseconds for waiting the consensus result
     */
    private long consensusWaitTime = 2 * 1000;

    /**
     * the private key of node
     */
    private String privateKey;

    /**
     * master public key
     */
    private String masterPubKey;

    /**
     * the node name prefix
     */
    private String prefix;
}
