/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveType;
import com.higgs.trust.consensus.atomix.core.primitive.ICommandPrimitive;
import com.higgs.trust.consensus.atomix.example.ExampleCommand;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.cluster.Member;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Slf4j @Service public class AtomixServer
    implements ConsensusStateMachine, ConsensusClient, ApplicationListener<ApplicationReadyEvent> {

    @Autowired private AtomixRaftProperties properties;

    @Autowired private AbstractCommitReplicateComposite replicateComposite;

    private ICommandPrimitive commandPrimitive;

    private Atomix atomix;

    @Override public void start() {
        List<Node> nodes = new ArrayList<>();
        AtomicReference<String> currentMember = new AtomicReference<>();
        properties.getCluster().forEach((key, value) -> {
            Member member = Member.builder().withAddress(value).withId(key).build();
            nodes.add(member);
            if (properties.getAddress().equals(value)) {
                currentMember.set(key);
            }
        });
        if (atomix == null) {
            //@formatter:off
            atomix = Atomix.builder()
                .withAddress(properties.getAddress())
                .withMemberId(currentMember.get())
                .withMembershipProvider(BootstrapDiscoveryProvider.builder().withNodes(nodes).build())
                .withManagementGroup(
                    RaftPartitionGroup.builder(properties.getSystemGroup())
                        .withStorageLevel(StorageLevel.DISK)
                        .withMembers(properties.getCluster().keySet())
                        .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getSystemGroup(), currentMember.get())))
                        .build())
                .withPartitionGroups(
                    RaftPartitionGroup.builder(properties.getGroup())
                        .withStorageLevel(StorageLevel.DISK)
                        .withMembers(properties.getCluster().keySet())
                        .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getGroup(), currentMember.get())))
                        .build())
                .build();
            //@formatter:on
            log.info("start atomix, with nodes:{}", nodes);
            atomix.start().join();
            commandPrimitive =
                atomix.primitiveBuilder(CommandPrimitiveType.instance().name(), CommandPrimitiveType.instance())
                    .build();
        }
    }

    @Override public void leaveConsensus() {

    }

    @Override public void joinConsensus() {

    }

    @Override public <T> CompletableFuture<Void> submit(AbstractConsensusCommand<T> command) {
        CompletableFuture<Void> submit = commandPrimitive.async().submit(command);
        return submit;
    }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
        start();

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    ExampleCommand command = new ExampleCommand(properties.getAddress() + UUID.randomUUID());
                    if (log.isDebugEnabled()) {
                        log.debug("submit command:{}", command.getMsg());
                    }
                    this.submit(command);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        });
    }
}

