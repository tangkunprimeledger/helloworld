/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.properties.master;

import com.higgs.trust.consensus.config.NodeState;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/6/13
 */
@Getter @Setter @ConfigurationProperties(prefix = "higgs.trust") @Configuration public class MasterConfig
    implements InitializingBean {

    /**
     * master name
     */
    private String masterName;

    @Autowired private NodeState nodeState;

    @Override public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(masterName)) {
            masterName = nodeState.getNodeName();
        }
        nodeState.changeMaster(masterName);
    }
}
