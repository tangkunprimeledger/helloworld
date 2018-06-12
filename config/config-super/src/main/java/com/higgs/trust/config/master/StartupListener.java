/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.config.node.listener.StateChangeListener;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.NodeState;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Component public class StartupListener implements StateChangeListener, InitializingBean {
    @Autowired private ChangeMasterService changeMasterService;
    @Autowired private NodeState nodeState;

    @Override public void stateChanged(NodeStateEnum from, NodeStateEnum to) {
        if (to == NodeStateEnum.Running) {
            changeMasterService.startHeartbeatTimeout();
        }
    }

    @Override public void afterPropertiesSet() {
        nodeState.registerStateListener(this);
    }
}
