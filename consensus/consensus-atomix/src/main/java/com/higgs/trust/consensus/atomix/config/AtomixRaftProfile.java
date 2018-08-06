/*
 * Copyright (c) 2013-2017, suimi
 */
//package com.higgs.trust.consensus.atomix.config;
//
//import io.atomix.cluster.MemberId;
//import io.atomix.core.AtomixConfig;
//import io.atomix.core.profile.Profile;
//import io.atomix.protocols.raft.partition.RaftPartitionGroupConfig;
//import io.atomix.storage.StorageLevel;
//
//import java.util.stream.Collectors;
//
///**
// * @author suimi
// * @date 2018/7/6
// */
//public class AtomixRaftProfile implements Profile {
//
//    private AtomixRaftProperties properties;
//
//    public AtomixRaftProfile(AtomixRaftProperties properties) {
//        this.properties = properties;
//    }
//
//    @Override public void configure(AtomixConfig config) {
//        //@formatter:off
//        config.setManagementGroup(new RaftPartitionGroupConfig().setName(properties.getSystemGroup())
//            .setPartitionSize((int)config.getClusterConfig()
//                .getMembers().stream().filter(member -> member.getId().type() == MemberId.Type.IDENTIFIED).count())
//            .setPartitions(1)
//            .setMembers(config.getClusterConfig()
//                .getMembers().stream()
////                .filter(member -> member.getId().type() == MemberId.Type.IDENTIFIED)
//                .map(node -> node.getId().id())
//                .collect(Collectors.toSet()))
//            .setStorageLevel(StorageLevel.DISK.name())
//            .setDataDirectory(String.format("%s/%s", properties.getDataPath(), properties.getSystemGroup())));
//        config.addPartitionGroup(new RaftPartitionGroupConfig()
//            .setName(properties.getGroup())
//            .setPartitionSize(properties.getPartitionSize())
//            .setPartitions(properties.getNumPartitions())
//            .setMembers(config.getClusterConfig()
//                .getMembers().stream()
////                .filter(member -> member.getId().type() == MemberId.Type.IDENTIFIED)
//                .map(node -> node.getId().id())
//                .collect(Collectors.toSet()))
//            .setStorageLevel(StorageLevel.DISK.name())
//            .setDataDirectory(String.format("%s/%s", properties.getDataPath(), properties.getGroup())));
//    }
//
//    @Override public String name() {
//        return properties.getName();
//    }
//}
