package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.example.StringValidCommand;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Slf4j
public class ReceiveStorageTest {
    private ValidCommandWrap validCommandWrap;
    private ReceiveStorage receiveStorage;

    @BeforeTest
    public void before() {
        StringValidCommand stringValidCommand = new StringValidCommand("test String command");
        validCommandWrap = ValidCommandWrap.of(stringValidCommand).fromNodeName("self");
        receiveStorage = ReceiveStorage.createMemoryStorage();
    }

    @Test
    public void testAdd() {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.add(key, validCommandWrap);
            assertNotNull(receiveCommandStatistics);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddException() {
        receiveStorage.openTx();
        try {
            receiveStorage.add(null, null);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testGetReceiveCommandStatistics() {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            receiveStorage.add(key, validCommandWrap);
            assertNotNull(receiveStorage.getReceiveCommandStatistics(key));
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testUpdateReceiveCommandStatistics() {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            assertNotNull(key);
            receiveStorage.add(key, validCommandWrap);
            ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);
            receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testUpdateReceiveCommandStatisticsException() {
        receiveStorage.openTx();
        try {
            receiveStorage.updateReceiveCommandStatistics(null, null);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testFromApplyQueue() {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            receiveStorage.add(key, validCommandWrap);
            receiveStorage.addApplyQueue(key);
            key = receiveStorage.takeFromApplyQueue();
            assertNotNull(key);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testAddDelayAndTrans() {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            receiveStorage.add(key, validCommandWrap);
            receiveStorage.addDelayQueue(key);
            key = receiveStorage.takeFromApplyQueue();
            assertNotNull(key);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testAddApply() {
        receiveStorage.openTx();
        try {
            for (int i = 0; i < 10; i++) {
                receiveStorage.addApplyQueue("test");
            }
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddApplyException() {
        receiveStorage.openTx();
        try {
            receiveStorage.addApplyQueue(null);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testAddDelayAndTransException() {
        receiveStorage.openTx();
        try {
            receiveStorage.addDelayQueue(null);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test
    public void testGC() throws InterruptedException {
        receiveStorage.openTx();
        try {
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            receiveStorage.add(key, validCommandWrap);
            receiveStorage.addGCSet(key);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testGCException() {
        receiveStorage.openTx();
        try {
            receiveStorage.addGCSet(null);
        } finally {
            receiveStorage.rollBack();
            receiveStorage.closeTx();
        }
    }

    @AfterTest
    public void after() throws InterruptedException {
        Thread.sleep(200);
    }
}