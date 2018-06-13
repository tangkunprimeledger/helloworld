/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.listener.StateChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Component public class StartupListener implements StateChangeListener{
    @Autowired private ChangeMasterService changeMasterService;

    @Override public void stateChanged(NodeStateEnum from, NodeStateEnum to) {
        if (to == NodeStateEnum.Running) {
            changeMasterService.startHeartbeatTimeout();
        }
    }
}
