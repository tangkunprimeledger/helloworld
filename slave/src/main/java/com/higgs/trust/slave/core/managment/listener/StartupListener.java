/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment.listener;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.managment.master.ChangeMasterService;
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
