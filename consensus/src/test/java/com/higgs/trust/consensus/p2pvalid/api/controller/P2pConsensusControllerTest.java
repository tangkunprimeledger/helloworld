package com.higgs.trust.consensus.p2pvalid.api.controller;

import com.higgs.trust.consensus.p2pvalid.example.StringValidConsensus;
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
    private StringValidConsensus stringValidConsensus;

    @Test()
    public void testReceiveCommand() throws Exception {
        BlockHeader header = new BlockHeader();
        header.setHeight(10L);
        header.setPreviousHash("abc");
        int num = 30;
        while (num > 0) {
            log.info("wait for service registry ........................");
            Thread.sleep(2000);
            ValidateCommand validateCommand = new ValidateCommand(header.getHeight()+ new Random().nextInt(1000), header);
            stringValidConsensus.submit(validateCommand);
            num--;
        }
        Thread.sleep(300000);
    }
}