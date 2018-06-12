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
     * the try times of self check
     */
    private int selfCheckTimes = 100;

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

    /**
     * the wait time for command consensus
     */
    private long consensusWaitTime = 800L;

    /**
     * the time of master heartbeat
     */
    private int masterHeartbeat = 1000;

    /**
     * the min ratio of change master to master heartbeat
     */
    private int changeMasterMinRatio = 2;

    /**
     * the max ratio of change master to master heartbeat
     */
    private int changeMasterMaxRatio = 3;

}
