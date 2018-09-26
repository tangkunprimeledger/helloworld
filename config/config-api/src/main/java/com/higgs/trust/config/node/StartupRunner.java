/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.node;

import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/13
 */
@ConditionalOnProperty(prefix = "higgs.trust", name = {"isSlave", "autoRunning"}, havingValue = "true", matchIfMissing = true)
@Order
@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {


    @Autowired
    NodeState nodeState;

    @Autowired
    private NodeProperties nodeProperties;

    @Override
    public void run(String... args) {
        try {
            nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.Initialize);
            nodeState.changeState(NodeStateEnum.Initialize, NodeStateEnum.StartingConsensus);
            nodeState.changeState(NodeStateEnum.StartingConsensus, NodeStateEnum.SelfChecking);
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
            if (nodeProperties.isStandby()) {
                nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Standby);
                return;
            }
            nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
        } catch (Exception e) {
            log.error("startup error:", e);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.STARTUP_FAILED, 1);
            nodeState.changeState(nodeState.getState(), NodeStateEnum.Offline);
        }
    }
}
