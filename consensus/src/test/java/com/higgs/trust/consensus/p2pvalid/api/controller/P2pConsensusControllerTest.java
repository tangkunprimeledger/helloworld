package com.higgs.trust.consensus.p2pvalid.api.controller;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.BaseTest;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Random;

/**
 *
 */
@Slf4j
public class P2pConsensusControllerTest extends BaseTest {

    @Autowired
    private ValidConsensus validConsensus;

    @Test
    public void testFastJSONDeSerialize() {
        ValidCommand validCommand = new StringValidCommand("test string valid command");
        Object object = JSON.parse(JSON.toJSONString(validCommand));
        log.info("object is {}", object);
    }

    @Test
    public void testReceiveCommand() throws Exception {
        String url = "http://localhost:7070/consensus/p2p/receive_command";
        int num = 10;
        while (num > 0) {
            ValidCommand validCommand = new StringValidCommand("test string valid command ======== " + new Random().nextInt(1000));
            log.info("wait for service registry ........................");
            Thread.sleep(10000);
            validConsensus.submit(validCommand);
            num--;
        }
    }
}