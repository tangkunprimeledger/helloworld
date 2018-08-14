/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.properties.master;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/6/13
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "higgs.trust")
@Configuration
public class MasterConfig {

    /**
     * master name
     */
    private String masterName;
}
