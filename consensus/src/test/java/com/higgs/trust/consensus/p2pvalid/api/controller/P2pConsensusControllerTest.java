package com.higgs.trust.consensus.p2pvalid.api.controller;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import com.higgs.trust.consensus.p2pvalid.example.slave.BlockHeader;
import com.higgs.trust.consensus.p2pvalid.example.slave.ValidateCommand;
import com.higgs.trust.consensus.p2pvalid.p2pBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Random;

/**
 *
 */
@Slf4j
public class P2pConsensusControllerTest extends p2pBaseTest {

    @Autowired
    private ValidConsensus validConsensus;

    @Test
    public void testFastJSONDeSerialize() {
        ValidCommand validCommand = new StringValidCommand("test string valid command");
        Object object = JSON.parse(JSON.toJSONString(validCommand));
        log.info("object is {}", object);
    }

    @Test()
    public void testReceiveCommand() throws Exception {
        BlockHeader header = new BlockHeader();
        header.setHeight(10L);
        header.setPreviousHash("abc");
        String url = "http://localhost:7070/consensus/p2p/receive_command";
        int num = 3;
        while (num > 0) {
            log.info("wait for service registry ........................");
            Thread.sleep(10000);
            ValidateCommand validateCommand = new ValidateCommand(header.getHeight()+ new Random().nextInt(1000), header);
            validConsensus.submit(validateCommand);
            num--;
        }
        Thread.sleep(30000);
    }
}