package com.higgs.trust.consensus.p2pvalid.api.controller;
import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.BaseTest;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;

@Slf4j
public class P2pConsensusControllerTest extends BaseTest{

    @Autowired
    private ValidConsensus validConsensus;

    @Test
    public void testFastJSONDeSerialize() {
        ValidCommand validCommand = new StringValidCommand("test string valid command");
        ValidCommandWrap validCommandWrap = ValidCommandWrap.of(validCommand)
                .fromNodeName("fromeNode")
                .messageDigest("messageDigest")
                .addToNodeNames(new ArrayList<String>(){{add("self");}})
                .sign("sign");
        JSON.parse(JSON.toJSONString(validCommandWrap));
    }

    @Test
    public void testReceiveCommand() throws Exception {
        String url = "http://localhost:7070/consensus/p2p/receive_command";
        ValidCommand validCommand = new StringValidCommand("test string valid command");
        while(true){
            log.info("wait for service registry ........................");
            Thread.sleep(20000);
            validConsensus.submit(validCommand);
        }
    }
}