package com.higgs.trust.consensus.copycat.core;

import com.higgs.trust.consensus.copycat.BftBaseTest;
import com.higgs.trust.consensus.copycat.example.StringCommand;
import com.higgs.trust.consensus.core.ConsensusClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ConsensusClientTest extends BftBaseTest{
    @Autowired
    private ConsensusClient consensusClient;

    @Test
    public void testConsensusClient(){
//        while(true){

            CompletableFuture completableFuture = consensusClient.submit(new StringCommand("test consensusClient with String command" + new Random().nextInt(10000)));
            try {
                completableFuture.get();
            } catch (Exception e) {
                log.error("submit error {}", e);
            }
//        }
    }
}