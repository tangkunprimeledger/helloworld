package com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ReceiveCommandStatisticsTest {

    private ValidCommand<?> validCommand;
    private ReceiveCommandStatistics receiveCommandStatistics;

    @BeforeTest
    public void beforTest(){
        validCommand = new ValidCommand<Integer>() {
            @Override
            public String messageDigest() {
                return String.valueOf(get());
            }
        };
        receiveCommandStatistics = ReceiveCommandStatistics.create(validCommand);
    }

    @Test
    public void testInstance(){
        assertNotNull(receiveCommandStatistics);
    }

    @Test
    public void testAddFromNodeName(){
        receiveCommandStatistics.addFromNode("from");
    }

    @Test
    public void testGetValidCommand(){
        assertNotNull(receiveCommandStatistics.getValidCommand());
    }

    @Test
    public void testGetFromNodeNameSet(){
        assertNotNull(receiveCommandStatistics.getFromNodeNameSet());
    }
}