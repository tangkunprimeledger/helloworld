package com.higgs.trust.config.node;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration @ConfigurationProperties(prefix = "higgs.trust") @Getter @Setter public class NodeProperties {

    /**
     * default node name
     */
    private static final String DEFAULT_NODE_NAME = "DefaultNode";

    private String runMode = "Normal";

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
     * the node name prefix
     */
    private String prefix;

    /**
     * the wait time for command consensus
     */
    private long consensusWaitTime = 800L;


    public boolean isMock() {
        return "Mock".equalsIgnoreCase(runMode);
    }

}
