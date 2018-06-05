/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment.master;

import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.managment.listener.MasterChangeListener;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Service public class MasterHeartbeatService implements MasterChangeListener, InitializingBean {
    @Autowired private NodeProperties nodeProperties;

    @Autowired private LogReplicateHandler logReplicateHandler;

    @Autowired private NodeState nodeState;

    private ScheduledFuture masterHeartbeatTimer;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("master heartbeat thread");
        thread.setDaemon(true);
        return thread;
    });

    public void startMasterHeartbeat() {
        log.info("start master heartbeat timeout");
        resetMasterHeartbeat();
    }

    public void resetMasterHeartbeat() {
        log.debug("reset master heartbeat timeout");
        if (masterHeartbeatTimer != null) {
            masterHeartbeatTimer.cancel(true);
        }
        long delay = nodeProperties.getMasterHeartbeat();
        masterHeartbeatTimer =
            executor.schedule(() -> logReplicateHandler.masterHeartbeat(), delay, TimeUnit.MILLISECONDS);
    }

    public void cancelMasterHeart() {
        log.debug("cancel master heartbeat timeout");
        if (masterHeartbeatTimer != null) {
            masterHeartbeatTimer.cancel(true);
        }
    }

    @Override public void masterChanged(String masterName) {
        if (nodeState.isMaster()) {
            startMasterHeartbeat();
        } else {
            cancelMasterHeart();
        }
    }

    @Override public void afterPropertiesSet() {
        nodeState.registerMasterListener(this);
    }
}
