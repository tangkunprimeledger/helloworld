package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;

import java.io.File;
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

    public ReceiveStorage(String receiveDBDir) {
        File file = new File(receiveDBDir);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
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
     * @param validCommandWrap
     * @param threshold
     * @return
     */
    public String add(ValidCommandWrap validCommandWrap, Integer threshold) {
        try {
            ValidCommand<?> validCommand = validCommandWrap.getValidCommand();
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            HTreeMap<String, ReceiveCommandStatistics> receiveStatisticsMap = getReceiveStatisticsHTreeMap();
            ReceiveCommandStatistics receiveCommandStatistics = receiveStatisticsMap.getOrDefault(key, ReceiveCommandStatistics.of(validCommand));
            receiveCommandStatistics.addFromNode(validCommandWrap.getFromNodeName());
            receiveStatisticsMap.put(key, receiveCommandStatistics);
            if (receiveCommandStatistics.getFromNodeNameSet().size() >= threshold) {
                log.info("from node set size {} > threshold {}, trigger apply", receiveCommandStatistics.getFromNodeNameSet().size(), threshold);
                addApplyQueue(key);
            }
            receiveDB.commit();
            return key;
        } catch (Exception e) {
            receiveDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

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
     * @param key
     * @return
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

    private void addApplyQueue(String key) {
        applyQueueLock.lock();
        try {
            BTreeMap<Long, String> applyQueue = getReceiveApplyQueue();

            Map.Entry<Long, String> lastEntry = applyQueue.lastEntry();
            if (null == lastEntry) {
                applyQueue.put(0L, key);
            } else {
                applyQueue.put(lastEntry.getKey() + 1, key);
            }
            applyQueueCondition.signal();
            receiveDB.commit();
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
