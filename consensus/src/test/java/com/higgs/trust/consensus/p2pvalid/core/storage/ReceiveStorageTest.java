package com.higgs.trust.consensus.p2pvalid.core.storage;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ReceiveStorageTest {
    private ValidCommandWrap validCommandWrap;
    private ReceiveStorage receiveStorage;
    private Integer applyThreshold;

    @BeforeTest
    public void before(){
        applyThreshold = 1;
        StringValidCommand stringValidCommand = new StringValidCommand("test String command");
        validCommandWrap = ValidCommandWrap.of(stringValidCommand).fromNodeName("self");
        receiveStorage = ReceiveStorage.createMemoryStorage();
    }

    @Test
    public void testAdd(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.add(key, validCommandWrap);
        assertNotNull(receiveCommandStatistics);
    }

    @Test
    public void testGetReceiveCommandStatistics(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        assertNotNull(receiveStorage.getReceiveCommandStatistics(key));
    }

    @Test
    public void testUpdateReceiveCommandStatistics(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        assertNotNull(key);
        receiveStorage.add(key, validCommandWrap);
        ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);
        receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
    }

    @Test
    public void testFromApplyQueue(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addApplyQueue(key);
        key = receiveStorage.takeFromApplyQueue();
        assertNotNull(key);
    }

    @Test
    public void testAddDelayAndTrans(){
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addDelayQueue(key);
        key = receiveStorage.takeFromApplyQueue();
        assertNotNull(key);
    }

    @Test
    public void testGC() throws InterruptedException {
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        receiveStorage.add(key, validCommandWrap);
        receiveStorage.addGCSet(key);
        Thread.sleep(5000);
    }

    @AfterTest
    public void after() throws InterruptedException {
        Thread.sleep(200);
    }
}