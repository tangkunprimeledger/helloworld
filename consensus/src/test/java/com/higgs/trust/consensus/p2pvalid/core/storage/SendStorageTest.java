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
        sendStorage.openTx();
        try{
            String key = sendStorage.submit(validCommandWrap);
            assertNotNull(key);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test
    public void testGetSendCommandStatistics(){
        sendStorage.openTx();
        try{
            String key = sendStorage.submit(validCommandWrap);
            assertNotNull(sendStorage.getSendCommandStatistics(key));
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test
    public void testAddAndTakeFromSendQueue(){
        sendStorage.openTx();
        try{
            String key = sendStorage.submit(validCommandWrap);
            sendStorage.addSendQueue(key);
            assertEquals(key, sendStorage.takeFromSendQueue());
        }finally {
            sendStorage.commit();
            sendStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddSendQueueException(){
        sendStorage.openTx();
        try{
            sendStorage.addSendQueue(null);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test
    public void testAddAndTransFromDelayQueue(){
        sendStorage.openTx();
        try{
            String key = sendStorage.submit(validCommandWrap);
            sendStorage.addDelayQueue(key);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddDelayQueueException(){
        sendStorage.openTx();
        try{
            sendStorage.addDelayQueue(null);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testUpdateSendCommandStatisticsException(){
        sendStorage.openTx();
        try{
            sendStorage.updateSendCommandStatics(null,null);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test
    public void testGC() {
        sendStorage.openTx();
        try{
            String key = sendStorage.submit(validCommandWrap);
            sendStorage.addGCSet(key);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testGCException() {
        sendStorage.openTx();
        try{
            sendStorage.addGCSet(null);
        }finally {
            sendStorage.rollBack();
            sendStorage.closeTx();
        }
    }

    @AfterTest
    public void after() throws InterruptedException {
        Thread.sleep(1000);
    }
}