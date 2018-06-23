/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.node;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/13
 */
@Order @Component @Slf4j public class StartupRunner implements CommandLineRunner {

    @Autowired NodeState nodeState;

    @Override public void run(String... args) {
        try {
            nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
            nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
        } catch (Exception e) {
            log.error("startup error:", e);
            nodeState.changeState(nodeState.getState(), NodeStateEnum.Offline);
        }
    }
}
