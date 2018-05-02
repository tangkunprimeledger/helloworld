package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.common.ConsensusAssert;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.SendCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cwy
 */
@Slf4j
public class SendStorage {

    /**
     * command submit map , store the submit command
     */
    private static final String COMMAND_SUBMIT_MAP = "command_submit_queue";
    private static final String COMMAND_SEND_QUEUE = "command_send_queue";
    private static final String COMMAND_GC_QUEUE = "command_gc_queue";
    private static final String COMMAND_DELAY_QUEUE = "command_delay_queue";
    private HTreeMap<String, SendCommandStatistics> submitMap;
    private BTreeMap<Long, String> sendQueue;
    private BTreeMap<Long, String> sendDelayQueue;
    private NavigableSet<String> gcSet;

    /**
     * transaction lock for send storage, guarantee isolate
     */
    private final Lock txLock;

    private DB sendDB;

    private Condition sendQueueCondition;

    private SendStorage(String sendDBDir) {
        this.sendDB = DBMaker
                .fileDB(sendDBDir)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        txLock = new ReentrantLock(true);
        sendQueueCondition = txLock.newCondition();
        initStorageMap();
        initThreadPool();
    }

    private SendStorage() {
        this.sendDB = DBMaker
                .memoryDB()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        txLock = new ReentrantLock(true);
        sendQueueCondition = txLock.newCondition();
        initStorageMap();
        initThreadPool();
    }

    public static SendStorage createFileStorage(String sendDBDir) {
        return new SendStorage(sendDBDir);
    }

    public static SendStorage createMemoryStorage() {
        return new SendStorage();
    }

    public void openTx(){
        txLock.lock();
    }

    public void closeTx(){
        txLock.unlock();
    }

    public void commit(){
        sendDB.commit();
    }

    public void rollBack(){
        sendDB.rollback();
    }

    @SuppressWarnings("unchecked")
    private void initStorageMap(){
        openTx();
        try{

            submitMap = sendDB.hashMap(COMMAND_SUBMIT_MAP)
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();

            sendQueue = sendDB.treeMap(COMMAND_SEND_QUEUE)
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(Serializer.STRING)
                    .createOrOpen();

            sendDelayQueue = sendDB.treeMap(COMMAND_DELAY_QUEUE)
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();

            gcSet = sendDB.treeSet(COMMAND_GC_QUEUE)
                    .serializer(Serializer.STRING)
                    .createOrOpen();
            commit();
            log.info("storage map init success");
        } finally {
            closeTx();
        }
    }

    private void initThreadPool() {
        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("send trans delay to send thread");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(this::transFromDelayToSendQueue, 2, 2, TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("send storage gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, 1, 1, TimeUnit.SECONDS);

        log.info("thread pool init success");
    }

    /**
     * @param validCommandWrap validCommandWrap
     * @return String
     */
    public String submit(ValidCommandWrap validCommandWrap) {
        ConsensusAssert.notNull(validCommandWrap);
        SendCommandStatistics sendCommandStatistics = SendCommandStatistics.of(validCommandWrap);
        String key = UUID.randomUUID().toString();
        while (submitMap.containsKey(key)){
            key = UUID.randomUUID().toString();
        }
        submitMap.put(key, sendCommandStatistics);
        return key;
    }

    public SendCommandStatistics getSendCommandStatistics(String key) {
        return submitMap.get(key);
    }

    /**
     * must call txLock.lock before call this method
     * @param key key of sendCommandStatistics
     */
    public void addSendQueue(String key) {
        ConsensusAssert.notNull(key);
        Map.Entry<Long, String> lastEntry = sendQueue.lastEntry();
        if (null == lastEntry) {
            sendQueue.put(0L, key);
        } else {
            sendQueue.put(lastEntry.getKey() + 1, key);
        }
        sendQueueCondition.signal();
    }

    /**
     * must call txLock.lock before this method
     * @return String
     */
    public String takeFromSendQueue() {
        try {
            Map.Entry<Long, String> firstEntry = sendQueue.firstEntry();
            while (null == firstEntry) {
                sendQueueCondition.await();
                firstEntry = sendQueue.firstEntry();
            }
            return firstEntry.getValue();
        } catch (Exception e) {
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * must call txLock.lock before this method
     * @return String
     */
    public void removeFromSendQueue() {
        try {
            sendQueue.remove(sendQueue.firstEntry().getKey());
        } catch (Exception e) {
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * @param key key of sendCommandStatistics
     */
    public void addDelayQueue(String key) {
        ConsensusAssert.notNull(key);
        Map.Entry<Long, String> lastEntry = sendDelayQueue.lastEntry();
        if (null == lastEntry) {
            sendDelayQueue.put(0L, key);
        } else {
            sendDelayQueue.put(lastEntry.getKey() + 1, key);
        }
    }

    private void transFromDelayToSendQueue() {
        openTx();
        try {
            Map.Entry<Long, String> entry = sendDelayQueue.firstEntry();
            if (null == entry) {
                return;
            }
            sendDelayQueue.remove(entry.getKey());
            addSendQueue(entry.getValue());
            log.info("trans {} from delay to send queue", entry.getValue());
            commit();
        } catch (Exception e) {
            log.error("{}", e);
            rollBack();
        }finally {
            closeTx();
        }
    }


    private void gc() {
        openTx();
        try {
            if (gcSet.size() == 0) {
                return;
            }
            Set<String> deleteKeys = new HashSet<>();
            for (String key : gcSet) {
                log.info("sendQueue gc {}", submitMap.get(key));
                submitMap.remove(key);
                deleteKeys.add(key);
            }
            gcSet.removeAll(deleteKeys);
            commit();
        } catch (Exception e) {
            log.error("{}", e);
            rollBack();
        }finally {
            closeTx();
        }
    }

    public void updateSendCommandStatics(String key, SendCommandStatistics sendCommandStatistics) {
        ConsensusAssert.notNull(key);
        ConsensusAssert.notNull(sendCommandStatistics);
        submitMap.put(key,sendCommandStatistics);
    }

    /**
     *
     * @param key key of sendCommandStatistics
     */
    public void addGCSet(String key) {
        ConsensusAssert.notNull(key);
        gcSet.add(key);
    }


}
