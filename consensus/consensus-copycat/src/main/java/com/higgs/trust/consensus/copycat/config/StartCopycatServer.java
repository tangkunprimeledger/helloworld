/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.copycat.config;

import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Slf4j @Component public class StartCopycatServer implements ConsensusStateMachine, DisposableBean {

    @Autowired private CopycatProperties copycatProperties;

    @Autowired private StateMachine stateMachine;

    private CopycatServer server;

    private void start(CopycatProperties properties, StateMachine stateMachine) {
        log.info("copycat server config : {}", toString());
        Address addressT = new Address(properties.getAddress());
        CopycatServer.Builder builder = CopycatServer.builder(addressT);
        builder.withStateMachine(() -> {
            try {
                return stateMachine;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.withTransport(
            NettyTransport.builder().withAcceptBacklog(Math.min(properties.getBacklog(), 1000)).withRequestTimeout(1500)
                .withThreads(properties.getNettyThreadNum()).build());
        builder.withElectionTimeout(Duration.ofMillis(properties.getElectionTimeout()))
            .withHeartbeatInterval(Duration.ofMillis(properties.getHeartbeatInterval()))
            .withSessionTimeout(Duration.ofMillis(properties.getSessionTimeout()));

        Storage storage = Storage.builder().withStorageLevel(StorageLevel.DISK).withDirectory(properties.getLogDir())
            .withMinorCompactionInterval(Duration.ofMillis(properties.getMinorCompactionInterval()))
            .withEntryBufferSize(properties.getEntryBufferSize())
            .withCompactionThreads(properties.getCompactionThreads())
            .withMaxEntriesPerSegment(properties.getMaxEntriesPerSegment())
            .withMajorCompactionInterval(Duration.ofMillis(properties.getMajorCompactionInterval()))
            .withCompactionThreshold(properties.getCompactionThreshold()).build();

        builder.withStorage(storage);
        server = builder.build();
        List<String> clusterList = Arrays.asList(properties.getCluster().split(","));
        List<Address> clusterAddress = new ArrayList<>();
        for (String addressStr : clusterList) {
            clusterAddress.add(new Address(StringUtils.trim(addressStr)));
        }
        log.info("copycat cluster start ...");
        server.bootstrap(clusterAddress);
    }

    @StateChangeListener(NodeStateEnum.Running) @Order public synchronized void start() {
        if (server == null) {
            start(copycatProperties, stateMachine);
        }
    }

    @Override public void leaveConsensus() {
        if (server != null) {
            server.leave();
        }
    }

    @Override public void joinConsensus() {

    }

    @Override public void initStart() {

    }

    @Override public void destroy() {
        if (server != null && server.isRunning()) {
            server.shutdown();
        }
    }
}
