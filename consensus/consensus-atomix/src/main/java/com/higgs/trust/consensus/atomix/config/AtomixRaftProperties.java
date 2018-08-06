package com.higgs.trust.consensus.atomix.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration @ConfigurationProperties(prefix = "copycat.server") @Getter @Setter @ToString public class AtomixRaftProperties {

    private String client;

    private String address;

    private String cluster;

    private String serverId;

    private Integer nettyThreadNum = 10;

    private String logDir = "copycat/logs";

    private Long minorCompactionInterval = 12000L;

    private Integer entryBufferSize = 200;

    private Integer compactionThreads = 4;

    private Integer maxEntriesPerSegment = 20000;

    private Long majorCompactionInterval = 60000L;

    private Double compactionThreshold = 0.01;

    private Long electionTimeout = 2000L;

    private Long heartbeatInterval = 500L;

    private Long sessionTimeout = 5000L;

    private Integer backlog = 1000;

    private String name = "consensus";

    private String systemGroup = "sys-group";

    private String group = "raft-group";

    private String dataPath = "/tmp/raft";

    private int partitionSize = 3;

    private int numPartitions = 1;
}
