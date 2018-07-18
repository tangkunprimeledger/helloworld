/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.ConsensusCommand;
import com.netflix.discovery.converters.Auto;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Slf4j @Service public class AtomixServer
    implements ConsensusStateMachine, ConsensusClient, ApplicationListener<ApplicationReadyEvent> {

    @Autowired private AtomixRaftProperties properties;

    @Autowired private AbstractCommitReplicateComposite replicateComposite;

    @Autowired private AtomixRaftProfile profile;

    private Atomix atomix;

    @Override public void start() {
        List<String> clusterList = Arrays.asList(properties.getCluster().split(","));
        List<Member> members = new ArrayList<>();
        for (String addressStr : clusterList) {
            members.add(Member.builder().withAddress(StringUtils.trim(addressStr)).build());
        }
        if (atomix == null) {
            atomix = Atomix.builder().withLocalMember(Member.builder().withAddress(properties.getAddress()).build())
                .withMembers(members).withProfiles(profile).build();
            log.info("start atomix, with members:{}", members);
            atomix.start().join();
        }
    }

    @Override public void leaveConsensus() {

    }

    @Override public void joinConsensus() {

    }

    @Override public <T> CompletableFuture<T> submit(ConsensusCommand<T> command) {
        atomix.getCommunicationService().broadcastIncludeSelf(command.getClass().getName(), command);
        return CompletableFuture.completedFuture(command.get());
    }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
        start();
        Map<Class<?>, Function<ConsensusCommit<?>, ?>> classFunctionMap = replicateComposite.registerCommit();
        classFunctionMap.entrySet().forEach(entry -> atomix.getCommunicationService()
            .subscribe(entry.getKey().getName(),
                o -> CompletableFuture.completedFuture(entry.getValue().apply((ConsensusCommit<?>)o))));
    }
}
