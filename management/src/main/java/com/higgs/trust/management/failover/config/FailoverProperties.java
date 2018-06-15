package com.higgs.trust.management.failover.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component @ConfigurationProperties(prefix = "higgs.trust.failover") @Getter @Setter public class FailoverProperties {
    /**
     * the threshold of package cache
     */
    private int threshold = 100;

    /**
     * the package size of keep  when package cache overstep of threshold
     */
    private int keepSize = 10;

    /**
     * the size of blockheader which get from other node
     */
    private int headerStep = 200;

    /**
     * the size of block which get from other node
     */
    private int blockStep = 50;

    /**
     * the try times for getting the block or blockheader from other node
     */
    private int tryTimes = 3;

    /**
     * failover step
     */
    private int failoverStep = 10;
}