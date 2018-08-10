package com.higgs.trust.consensus.atomix.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration @ConfigurationProperties(prefix = "atomix") @Getter @Setter @ToString public class AtomixRaftProperties {

    private String address;

    private Map<String,String> cluster;

    private String systemGroup = "sys-group";

    private String group = "raft-group";

    private String dataPath = "/tmp/raft";

    private int partitionSize = 4;

    private int numPartitions = 1;
}
