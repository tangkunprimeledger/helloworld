package com.higgs.trust.consensus.copycat.example;

import com.higgs.trust.consensus.core.ConsensusClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class ClientExample {
    @Autowired
    private ConsensusClient consensusClient;

    @PostConstruct
    public void testClient() {
        System.out.println(consensusClient);
    }
}
