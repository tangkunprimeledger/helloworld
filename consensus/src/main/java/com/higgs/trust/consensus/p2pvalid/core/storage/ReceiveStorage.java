package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;

import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private DB receiveDB;

    private ReentrantLock applyQueueLock;

    private Condition applyQueueCondition;

    private ReceiveStorage(String receiveDBDir) {
        this.receiveDB = DBMaker
                .fileDB(receiveDBDir)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        applyQueueLock = new ReentrantLock();
        applyQueueCondition = applyQueueLock.newCondition();
        initThreadPool();
    }

    private ReceiveStorage() {
        this.receiveDB = DBMaker
                .memoryDB()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        applyQueueLock = new ReentrantLock();
        applyQueueCondition = applyQueueLock.newCondition();
        initThreadPool();
    }

    public static ReceiveStorage createFileStorage(String receiveDBDir){
        return new ReceiveStorage(receiveDBDir);
    }

    public static  ReceiveStorage createMemoryStorage(){
        return new ReceiveStorage();
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
        }).scheduleWithFixedDelay(this::gc, 2, 2, TimeUnit.SECONDS);
    }

    /**
     * add commandWrap and return the key
     * @param key key of the command
     * @param validCommandWrap wrap of command
     * @return ReceiveCommandStatistics
     */
    public ReceiveCommandStatistics add(String key, ValidCommandWrap validCommandWrap) {
        applyQueueLock.lock();
        try {
            ValidCommand<?> validCommand = validCommandWrap.getValidCommand();
            HTreeMap<String, ReceiveCommandStatistics> receiveStatisticsMap = getReceiveStatisticsHTreeMap();
            ReceiveCommandStatistics receiveCommandStatistics = receiveStatisticsMap.getOrDefault(key, ReceiveCommandStatistics.create(validCommand));

            if(receiveCommandStatistics.getFromNodeNameSet().contains(validCommandWrap.getFromNodeName())){
                log.info("duplicate fromNodeName {} in fromNodeNameSet {}", validCommandWrap.getFromNodeName(), receiveCommandStatistics.getFromNodeNameSet());
                return receiveCommandStatistics;
            }

            receiveCommandStatistics.addFromNode(validCommandWrap.getFromNodeName());
            receiveStatisticsMap.put(key, receiveCommandStatistics);
            receiveDB.commit();
            return receiveCommandStatistics;
        } catch (Exception e) {
            receiveDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        }finally {
            applyQueueLock.unlock();
        }
    }

    /**
     *  update receiveCommandStatistics
     * @param key key of receiveCommandStatistics
     * @param receiveCommandStatistics receiveCommandStatistics
     */
    public void updateReceiveCommandStatistics(String key, ReceiveCommandStatistics receiveCommandStatistics) {
        try {
            HTreeMap<String, ReceiveCommandStatistics> commandStatisticsMap = getReceiveStatisticsHTreeMap();
            commandStatisticsMap.put(key, receiveCommandStatistics);
            receiveDB.commit();
        } catch (Exception e) {
            receiveDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * get commandStatistics by key
     * @param key key of receiveCommandStatistics
     * @return ReceiveCommandStatistics
     */
    public ReceiveCommandStatistics getReceiveCommandStatistics(String key) {
        try {
            HTreeMap<String, ReceiveCommandStatistics> receiveStatisticsMap = getReceiveStatisticsHTreeMap();
            return receiveStatisticsMap.get(key);
        } catch (Exception e) {
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * add the key to the apply queue
     * @param key
     */
    public void addApplyQueue(String key) {
        applyQueueLock.lock();
        try {
            BTreeMap<Long, String> applyQueue = getReceiveApplyQueue();

            Map.Entry<Long, String> lastEntry = applyQueue.lastEntry();
            if (null == lastEntry) {
                applyQueue.put(0L, key);
            } else {
                applyQueue.put(lastEntry.getKey() + 1, key);
            }
            receiveDB.commit();
            applyQueueCondition.signal();
        } catch (Exception e) {
            receiveDB.rollback();
            log.error("{}", e);
        } finally {
            applyQueueLock.unlock();
        }
    }

    public String takeFromApplyQueue() {
        applyQueueLock.lock();
        try {
            BTreeMap<Long, String> retryQueue = getReceiveApplyQueue();
            Map.Entry<Long, String> entry = retryQueue.firstEntry();
            while (null == entry) {
                applyQueueCondition.await(5, TimeUnit.SECONDS);
                entry = retryQueue.firstEntry();
            }
            retryQueue.remove(entry.getKey());
            receiveDB.commit();
            return entry.getValue();
        } catch (Exception e) {
            receiveDB.rollback();
            throw new RuntimeException(e);
        } finally {
            applyQueueLock.unlock();
        }
    }

    public void addDelayQueue(String key) {
        try {
            BTreeMap<Long, String> delayQueue = getReceiveDelayQueue();

            Map.Entry<Long, String> lastEntry = delayQueue.lastEntry();
            if (null == lastEntry) {
                delayQueue.put(0L, key);
            } else {
                delayQueue.put(lastEntry.getKey() + 1, key);
            }
            receiveDB.commit();
        } catch (Exception e) {
            receiveDB.rollback();
            log.error("{}", e);
        }
    }

    private void transFromDelayToApplyQueue() {
        try {
            BTreeMap<Long, String> delayQueue = getReceiveDelayQueue();
            Map.Entry<Long, String> entry = delayQueue.firstEntry();
            if (null == entry) {
                return;
            }
            delayQueue.remove(entry.getKey());
            addApplyQueue(entry.getValue());
            log.info("trans {} from delay to apply queue", entry.getValue());
            receiveDB.commit();
        } catch (Exception e) {
            receiveDB.rollback();
            throw new RuntimeException(e);
        }
    }

    private void gc() {
        try {
            HTreeMap<String, ReceiveCommandStatistics> receiveStatisticsMap = getReceiveStatisticsHTreeMap();
            NavigableSet<String> gcSet = getReceiveGCSet();
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
            receiveDB.commit();
        } catch (Exception e) {
            log.error("{}", e);
            receiveDB.rollback();
        }
    }

    public void addGCSet(String key) {
        try {
            NavigableSet<String> gcSet = getReceiveGCSet();
            gcSet.add(key);
            receiveDB.commit();
        } catch (Exception e) {
            receiveDB.rollback();
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    private HTreeMap<String, ReceiveCommandStatistics> getReceiveStatisticsHTreeMap() {
        return receiveDB.hashMap(RECEIVE_STATISTICS_MAP)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    private BTreeMap<Long, String> getReceiveApplyQueue() {
        return receiveDB.treeMap(RECEIVE_APPLY_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    private BTreeMap<Long, String> getReceiveDelayQueue() {
        return receiveDB.treeMap(RECEIVE_DELAY_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    private NavigableSet<String> getReceiveGCSet() {
        return receiveDB.treeSet(RECEIVE_GC_QUEUE)
                .serializer(Serializer.STRING)
                .createOrOpen();
    }
}
