package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.common.ConsensusAssert;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;

import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cwy
 */
@Slf4j
public class ReceiveStorage {

    private static final String RECEIVE_STATISTICS_MAP = "receive_statistics_map";

    private static final String RECEIVE_APPLY_QUEUE = "receive_apply_queue";

    /**
     * delay queue to add apply queue
     */
    private static final String RECEIVE_DELAY_QUEUE = "receive_delay_queue";

    private static final String RECEIVE_GC_QUEUE = "receive_gc_queue";

    private HTreeMap<String, ReceiveCommandStatistics> receiveStatisticsMap;

    private BTreeMap<Long, String> receiveApplyQueue;

    private BTreeMap<Long, String> receiveDelayQueue;

    private NavigableSet<String> gcSet;

    private DB receiveDB;

    private ReentrantLock txLock;

    private Condition applyQueueCondition;

    private ReceiveStorage(String receiveDBDir) {
        this.receiveDB = DBMaker
                .fileDB(receiveDBDir)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        txLock = new ReentrantLock();
        applyQueueCondition = txLock.newCondition();
        initStorageMap();
        initThreadPool();
    }

    private ReceiveStorage() {
        this.receiveDB = DBMaker
                .memoryDB()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        txLock = new ReentrantLock();
        applyQueueCondition = txLock.newCondition();
        initStorageMap();
        initThreadPool();
    }

    public static ReceiveStorage createFileStorage(String receiveDBDir){
        return new ReceiveStorage(receiveDBDir);
    }

    public static  ReceiveStorage createMemoryStorage(){
        return new ReceiveStorage();
    }


    public void openTx(){
        txLock.lock();
    }

    public void closeTx(){
        txLock.unlock();
    }

    public void commit(){
        receiveDB.commit();
    }

    private void compact(){
        openTx();
        try{
            receiveDB.getStore().compact();
            log.info("compact the store of the receive storage");
        }catch (Throwable throwable){
            log.error("{}", throwable);
        }finally {
            closeTx();
        }

    }

    public void rollBack(){
        receiveDB.rollback();
    }


    @SuppressWarnings("unchecked")
    private void initStorageMap(){
        openTx();
        try{
            receiveStatisticsMap = getReceiveStatisticsMap();
            receiveApplyQueue = getReceiveApplyQueue();
            receiveDelayQueue = getReceiveDelayQueue();
            gcSet = getGcSet();
            commit();
            log.info("storage map init success");
        }finally {
            closeTx();
        }
    }

    private void initThreadPool() {
        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("receive trans delay to apply thread");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(this::transFromDelayToApplyQueue, 2, 2, TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("receive storage gc thread");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(this::gc, 20, 20, TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("receive storage gc thread");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(this::compact, 60, 60, TimeUnit.SECONDS);
        log.info("thread pool init success");
    }

    /**
     * add commandWrap and return the key
     * @param key key of the command
     * @param validCommandWrap wrap of command
     * @return ReceiveCommandStatistics
     */
    public ReceiveCommandStatistics add(String key, ValidCommandWrap validCommandWrap) {
        receiveStatisticsMap = getReceiveStatisticsMap();
        ValidCommand<?> validCommand = validCommandWrap.getValidCommand();
        ReceiveCommandStatistics receiveCommandStatistics = receiveStatisticsMap.getOrDefault(key, ReceiveCommandStatistics.create(validCommand));

        receiveCommandStatistics.setTraceId(validCommandWrap.getTraceId());
        if(receiveCommandStatistics.getFromNodeNameSet().contains(validCommandWrap.getFromNodeName())){
            log.info("duplicate fromNodeName {} in fromNodeNameSet {}", validCommandWrap.getFromNodeName(), receiveCommandStatistics.getFromNodeNameSet());
            return receiveCommandStatistics;
        }

        receiveCommandStatistics.addFromNode(validCommandWrap.getFromNodeName());
        receiveStatisticsMap.put(key, receiveCommandStatistics);
        return receiveCommandStatistics;
    }

    /**
     *  update receiveCommandStatistics
     * @param key key of receiveCommandStatistics
     * @param receiveCommandStatistics receiveCommandStatistics
     */
    public void updateReceiveCommandStatistics(String key, ReceiveCommandStatistics receiveCommandStatistics) {
        ConsensusAssert.notNull(key);
        ConsensusAssert.notNull(receiveCommandStatistics);
        receiveStatisticsMap = getReceiveStatisticsMap();
        receiveStatisticsMap.put(key, receiveCommandStatistics);
    }

    /**
     * get commandStatistics by key
     * @param key key of receiveCommandStatistics
     * @return ReceiveCommandStatistics
     */
    public ReceiveCommandStatistics getReceiveCommandStatistics(String key) {
        ConsensusAssert.notNull(key);
        receiveStatisticsMap = getReceiveStatisticsMap();
        return receiveStatisticsMap.get(key);
    }

    /**
     * add the key to the apply queue
     * @param key
     */
    public void addApplyQueue(String key) {
        ConsensusAssert.notNull(key);
        receiveApplyQueue = getReceiveApplyQueue();
        Map.Entry<Long, String> lastEntry = receiveApplyQueue.lastEntry();
        if (null == lastEntry) {
            receiveApplyQueue.put(0L, key);
        } else {
            receiveApplyQueue.put(lastEntry.getKey() + 1, key);
        }
        applyQueueCondition.signal();
    }


    public String getFirstFromApplyQueue() throws InterruptedException {
        receiveApplyQueue = getReceiveApplyQueue();
        Map.Entry<Long, String> entry = receiveApplyQueue.firstEntry();
        while (null == entry) {
            applyQueueCondition.await(5, TimeUnit.SECONDS);
            entry = receiveApplyQueue.firstEntry();
        }
        return entry.getValue();
    }

    public void deleteFirstFromApplyQueue(){
        receiveApplyQueue = getReceiveApplyQueue();
        receiveApplyQueue.remove(receiveApplyQueue.firstKey());
    }


    public void addDelayQueue(String key) {
        ConsensusAssert.notNull(key);
        receiveDelayQueue = getReceiveDelayQueue();
        Map.Entry<Long, String> lastEntry = receiveDelayQueue.lastEntry();
        if (null == lastEntry) {
            receiveDelayQueue.put(0L, key);
        } else {
            receiveDelayQueue.put(lastEntry.getKey() + 1, key);
        }
    }

    private void transFromDelayToApplyQueue() {
        openTx();
        try {
            receiveDelayQueue = getReceiveDelayQueue();
            Map.Entry<Long, String> entry = receiveDelayQueue.firstEntry();
            if (null == entry) {
                return;
            }
            receiveDelayQueue.remove(entry.getKey());
            addApplyQueue(entry.getValue());
            log.info("trans {} from delay to apply queue", entry.getValue());
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
            gcSet = getGcSet();
            receiveStatisticsMap = getReceiveStatisticsMap();
            if (gcSet.size() == 0) {
                return;
            }
            Set<String> deleteKeys = new HashSet<>();
            for (String key : gcSet) {
                log.info("receive gc {}", receiveStatisticsMap.get(key));
                receiveStatisticsMap.remove(key);
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

    public void addGCSet(String key) {
        try {
            gcSet = getGcSet();
            gcSet.add(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HTreeMap getReceiveStatisticsMap(){
        return receiveDB.hashMap(RECEIVE_STATISTICS_MAP)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    private BTreeMap getReceiveApplyQueue(){
        return receiveDB.treeMap(RECEIVE_APPLY_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    private BTreeMap getReceiveDelayQueue(){
        return receiveDB.treeMap(RECEIVE_DELAY_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    private NavigableSet<String> getGcSet(){
        return receiveDB.treeSet(RECEIVE_GC_QUEUE)
                .serializer(Serializer.STRING)
                .createOrOpen();
    }
}
