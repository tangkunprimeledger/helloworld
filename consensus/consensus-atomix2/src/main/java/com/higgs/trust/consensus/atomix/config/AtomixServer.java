/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveType;
import com.higgs.trust.consensus.atomix.core.primitive.IAsyncCommandPrimitive;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import io.atomix.cluster.Node;
import io.atomix.cluster.NodeId;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.RaftProtocol;
import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.protocols.raft.proxy.CommunicationStrategy;
import io.atomix.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
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
@Slf4j @Service public class AtomixServer
    implements ConsensusStateMachine, ConsensusClient, ApplicationListener<ApplicationReadyEvent> {

    @Autowired private AtomixRaftProperties properties;

    @Autowired private CommandPrimitiveType primitiveType;

    private Atomix atomix;

    private IAsyncCommandPrimitive primitive;

    @Override public void start() {
        AtomicReference<Node> localNode = new AtomicReference<>();
        List<Node> nodes = new ArrayList<>();
        properties.getCluster().forEach((key, value) -> {
            Node member = Node.builder().withType(Node.Type.CORE).withAddress(value).withId(NodeId.from(key)).build();
            nodes.add(member);
            if (value.equals(properties.getAddress())) {
                localNode.set(member);
            }
        });
        if (atomix == null) {
            //@formatter:off
            atomix = Atomix.builder()
                .withLocalNode(localNode.get())
                .withNodes(nodes)
                .withPrimitiveTypes(primitiveType)
                .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getSystemGroup(), localNode.get().id().toString())))
                .withPartitionGroups(
                    RaftPartitionGroup.builder(properties.getGroup())
                        .withStorageLevel(StorageLevel.DISK)
                        .withNumPartitions(properties.getNumPartitions())
                        .withPartitionSize(properties.getPartitionSize())
                        .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(),
                            properties.getGroup(), localNode.get().id().toString())))
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
        CompletableFuture<Void> submit = primitive.submit(command);
        return submit;
    }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
        start();
        primitive = atomix.primitiveBuilder("my-primitive", primitiveType).withProtocol(
            RaftProtocol.builder(properties.getGroup()).withReadConsistency(ReadConsistency.LINEARIZABLE)
                .withCommunicationStrategy(CommunicationStrategy.LEADER).build()).build().async();
    }
}

