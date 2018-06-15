package com.higgs.trust.consensus.copycat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration @ConfigurationProperties(prefix = "copycat.server") @Getter @Setter public class CopycatProperties {

    private String client;

    private String address;

    private String cluster;

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
}
