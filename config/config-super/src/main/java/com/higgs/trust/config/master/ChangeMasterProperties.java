/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/6/14
 */
@Configuration @ConfigurationProperties(prefix = "higgs.trust.master") @Getter @Setter
public class ChangeMasterProperties {

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
