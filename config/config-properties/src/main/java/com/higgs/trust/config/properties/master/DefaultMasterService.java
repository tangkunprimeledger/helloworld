/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.properties.master;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author suimi
 * @date 2018/7/12
 */
@Service public class DefaultMasterService {

    @Autowired private NodeState nodeState;

    @Autowired private MasterConfig config;

    @StateChangeListener(value = NodeStateEnum.Running, before = true) public void setMaster() {
        String masterName = config.getMasterName();
        if (StringUtils.isBlank(masterName)) {
            masterName = nodeState.getNodeName();
        }
        nodeState.changeMaster(masterName);
    }
}
