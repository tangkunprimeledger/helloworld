package com.higgs.trust.consensus.bft.core;

import com.higgs.trust.consensus.bft.BftBaseTest;
import com.higgs.trust.consensus.bft.example.StringCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ConsensusClientTest extends BftBaseTest{
    @Autowired
    private ConsensusClient consensusClient;

    @Test
    public void testConsensusClient(){
        CompletableFuture completableFuture = consensusClient.submit(new StringCommand("test consensusClient with String command"));
    }
}