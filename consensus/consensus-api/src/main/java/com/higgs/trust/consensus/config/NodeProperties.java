package com.higgs.trust.consensus.config;

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
     * the try times at start up
     */
    private int startupRetryTime = 100;

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
    private long consensusWaitTime = 1000L;

    /**
     * mark whether the trust is standby
     */
    private boolean standby = false;



    /**
     * trust path
     */
    private String path;

    public boolean isMock() {
        return "Mock".equalsIgnoreCase(runMode);
    }

}
