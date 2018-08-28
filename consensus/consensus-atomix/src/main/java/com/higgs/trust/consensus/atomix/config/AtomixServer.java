/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveType;
import com.higgs.trust.consensus.atomix.core.primitive.ICommandPrimitive;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.cluster.Member;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import io.atomix.core.AtomixRegistry;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Slf4j @Service public class AtomixServer implements ConsensusStateMachine, ConsensusClient {

    @Autowired private AtomixRaftProperties properties;

    @Autowired private CommandPrimitiveType primitiveType;

    @Autowired private AtomixRegistry atomixRegistry;

    @Autowired private AtomixConfig atomixConfig;

    private Atomix atomix;

    @StateChangeListener(NodeStateEnum.Running) @Order @Override public synchronized void start() {
        List<Node> nodes = new ArrayList<>();
        AtomicReference<String> currentMember = new AtomicReference<>();
        properties.getCluster().forEach((key, value) -> {
            Member member = Member.builder().withAddress(value).withId(key).build();
            nodes.add(member);
            if (properties.getAddress().trim().equals(value.trim())) {
                currentMember.set(key);
            }
        });
        if (atomix == null) {
            //@formatter:off
            String localMemberId = currentMember.get();
            atomix = new CustomAtomixBuilder(atomixConfig,atomixRegistry)
                .withAddress(properties.getAddress())
                .withMemberId(localMemberId)
                .withMembershipProvider(BootstrapDiscoveryProvider.builder().withNodes(nodes).build())
                .withShutdownHookEnabled()
                .withManagementGroup(
                    RaftPartitionGroup.builder(properties.getSystemGroup())
                        .withStorageLevel(StorageLevel.MAPPED)
                        .withSegmentSize(properties.getSegmentSize())
                        .withMaxEntrySize(properties.getMaxEntrySize())
                        .withNumPartitions(properties.getNumPartitions())
                        .withPartitionSize(properties.getPartitionSize())
                        .withMembers(properties.getCluster().keySet())
                        .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getSystemGroup(), localMemberId)))
                        .build())
                .withPartitionGroups(
                    RaftPartitionGroup.builder(properties.getGroup())
                        .withStorageLevel(StorageLevel.MAPPED)
                        .withSegmentSize(properties.getSegmentSize())
                        .withMaxEntrySize(properties.getMaxEntrySize())
                        .withMembers(properties.getCluster().keySet())
                        .withNumPartitions(properties.getNumPartitions())
                        .withPartitionSize(properties.getPartitionSize())
                        .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getGroup(), localMemberId)))
                        .build())
                .build();
            //@formatter:on
            log.info("start atomix, with nodes:{}", nodes);
            atomix.start().join();
        }
    }

    @Override public void leaveConsensus() {
    }

    @Override public void joinConsensus() {

    }

    @Override public <T> CompletableFuture<Void> submit(AbstractConsensusCommand<T> command) {
        ICommandPrimitive primitive = atomix.getPrimitive(primitiveType.name(), primitiveType);
        CompletableFuture<Void> submit = primitive.async().submit(command);
        return submit;
    }
}

