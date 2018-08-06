/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.example.ExampleCommand;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.ConsensusCommand;
import io.atomix.cluster.Member;
import io.atomix.cluster.MemberId;
import io.atomix.core.Atomix;
import io.atomix.core.value.AsyncAtomicValue;
import io.atomix.core.value.AtomicValue;
import io.atomix.protocols.raft.MultiRaftProtocol;
import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.protocols.raft.session.CommunicationStrategy;
import io.atomix.storage.StorageLevel;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Namespaces;
import io.atomix.utils.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Slf4j
@Service
public class AtomixServer
        implements ConsensusStateMachine, ConsensusClient, ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private AtomixRaftProperties properties;

    @Autowired
    private AbstractCommitReplicateComposite replicateComposite;

    //@Autowired private AtomixRaftProfile profile;

    private Atomix atomix;

    private Serializer serializer;

    @Override
    public void start() {
        List<String> clusterList = Arrays.asList(properties.getCluster().split(","));
        List<Member> members = new ArrayList<>();
        for (String addressStr : clusterList) {
            members.add(Member.builder().withAddress(StringUtils.trim(addressStr)).build());
        }
        if (atomix == null) {
            //TODO do configuration by using Profile
            Member member = Member.builder()
                    .withId(properties.getServerId())
                    .withAddress(properties.getAddress())
                    .build();
            atomix = Atomix.builder()
                    .withLocalMember(member)
                    .withMembers(member)//集群只有一个节点
                    //.withProfiles(profile)
                    .withManagementGroup(
                            RaftPartitionGroup.builder(properties.getSystemGroup())
                                    .withStorageLevel(StorageLevel.DISK).withMembers(properties.getServerId())
                                    .withNumPartitions(1)//系统集群只有一个节点
                                    .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(), properties.getSystemGroup(),properties.getServerId())))
                                    .build())
                    .withPartitionGroups(
                            RaftPartitionGroup.builder(properties.getGroup())
                                    .withStorageLevel(StorageLevel.DISK)
                                    .withMembers(properties.getServerId())
                                    .withNumPartitions(1)//raft集群只有一个节点
                                    .withDataDirectory(new File(String.format("%s/%s/%s", properties.getDataPath(), properties.getGroup(),properties.getServerId())))
                                    .build())
                    .build();

            log.info("start atomix, with members:{}", members);
            atomix.start().join();
        }
    }

    @Override
    public void leaveConsensus() {

    }

    @Override
    public void joinConsensus() {

    }

    @Override
    public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        atomix.getCommunicationService().broadcastIncludeSelf(command.getClass().getName(), command, serializer::encode);
        return CompletableFuture.completedFuture(command.get());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        start();

        //对command做序列化
        serializer = Serializer.using(Namespace.builder()
                .register(Namespaces.BASIC)
                .register(MemberId.class)
                .register(ExampleCommand.class)
                .register(CompletableFuture.class)
                .build());

        Map<Class<?>, Function<ConsensusCommit<?>, ?>> classFunctionMap= replicateComposite.registerCommit();
        classFunctionMap.entrySet().forEach(entry -> log.info("classFunctionMap Key Map:{}", entry.getKey().getName()));

        //CommunicationService直接传command方式,不走共识算法
        classFunctionMap.entrySet().forEach(entry -> {
            atomix.getCommunicationService()
                    .subscribe(//如果要序列化自定的command，传入serializer的同时，也要传入线程池
                            entry.getKey().getName(),
                            serializer::decode,
                            o -> {
                                Function function = entry.getValue();
                                function.apply(o);
                                return  CompletableFuture.completedFuture(true);
                            },
                            serializer::encode,
                            Executors.newFixedThreadPool(3));
        });

        //用atomix提供的primitive传command方式
        AsyncAtomicValue<ExampleCommand> value = atomix.<ExampleCommand>atomicValueBuilder("command-value")
                .withProtocol(MultiRaftProtocol.builder("raft")
                        .withReadConsistency(ReadConsistency.LINEARIZABLE)
                        .withCommunicationStrategy(CommunicationStrategy.LEADER)
                        .build())
                .withSerializer(serializer)
                .build().async();

        value.addListener(
            event1 ->{
                AbstractConsensusCommand command =event1.newValue();
                if (classFunctionMap.containsKey(command.getClass())) {
                    Function function = classFunctionMap.get(command.getClass());
                    function.apply(command);
                }

            }
        );

        //测试往atomix集群发command
        new Thread(() ->
        {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //communicationService发command到所有节点
            submit(new ExampleCommand("This is command submit from sender " + properties.getServerId()));

            //atomix提供的primitive装command共识到所有节点
            value.getAndSet(new ExampleCommand("This is command 1.1 value-set from sender " + properties.getServerId()));
            value.getAndSet(new ExampleCommand("This is command 2 value-set from sender " + properties.getServerId()));

        }).start();

    }
}

