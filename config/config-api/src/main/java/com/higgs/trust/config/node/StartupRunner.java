/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/13
 */
@Component @Slf4j public class StartupRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired NodeState nodeState;

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
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
