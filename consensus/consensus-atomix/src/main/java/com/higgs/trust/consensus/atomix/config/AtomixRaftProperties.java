package com.higgs.trust.consensus.atomix.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration @ConfigurationProperties(prefix = "atomix") @Getter @Setter @ToString public class AtomixRaftProperties {

    private String address;

    private Map<String, String> cluster;

    private String systemGroup = "sys-group";

    private String group = "raft-group";

    private String dataPath = "/tmp/raft";

    private int partitionSize = 6;

    private int numPartitions = 1;

    private int maxEntrySize = 1024 * 1024;

    /**
     * the maximum segment size in bytes
     */
    private int segmentSize = 1024 * 1024 * 10;
}
