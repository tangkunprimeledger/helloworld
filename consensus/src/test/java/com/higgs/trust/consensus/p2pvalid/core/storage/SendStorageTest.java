package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SendStorageTest {

    private ValidCommandWrap validCommandWrap;
    private SendStorage sendStorage;

    @BeforeTest
    public void before(){
        StringValidCommand stringValidCommand = new StringValidCommand("test String command");
        validCommandWrap = ValidCommandWrap.of(stringValidCommand);
        sendStorage = SendStorage.createMemoryStorage();
    }

    @Test
    public void testSend(){
        String key = sendStorage.submit(validCommandWrap);
        assertNotNull(key);
    }

    @Test
    public void testGetSendCommandStatistics(){
        String key = sendStorage.submit(validCommandWrap);
        assertNotNull(sendStorage.getSendCommandStatistics(key));
    }

    @Test
    public void testAddAndTakeFromSendQueue(){
        String key = sendStorage.submit(validCommandWrap);
        sendStorage.addSendQueue(key);
        assertEquals(key, sendStorage.takeFromSendQueue());
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddSendQueueException(){
        sendStorage.addSendQueue(null);
    }

    @Test
    public void testAddAndTransFromDelayQueue(){
        String key = sendStorage.submit(validCommandWrap);
        sendStorage.addDelayQueue(key);
        assertEquals(key, sendStorage.takeFromSendQueue());
    }

    @Test
    public void testAddDelayQueueException(){
        sendStorage.addDelayQueue(null);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testUpdateSendCommandStatisticsException(){
        sendStorage.updateSendCommandStatics(null,null);
    }

    @Test
    public void testGC() throws InterruptedException {
        String key = sendStorage.submit(validCommandWrap);
        sendStorage.addGCSet(key);
        Thread.sleep(5000);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testGCException() {
        sendStorage.addGCSet(null);
    }

    @AfterTest
    public void after() throws InterruptedException {
        Thread.sleep(200);
    }
}