package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.SendCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
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

    private DB sendDB;

    private ReentrantLock sendQueueLock;

    private Condition sendQueueCondition;

    public SendStorage(String sendDBDir) {
        this.sendDB = DBMaker
                .fileDB(sendDBDir)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .cleanerHackEnable()
                .transactionEnable()
                .make();
        sendQueueLock = new ReentrantLock();
        sendQueueCondition = sendQueueLock.newCondition();
        initThreadPool();
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
    }

    public synchronized String submit(ValidCommandWrap validCommandWrap) {
        try {
            HTreeMap<String, SendCommandStatistics> submitMap = getSubmitMap(sendDB);
            SendCommandStatistics sendCommandStatistics = SendCommandStatistics.of(validCommandWrap);
            String key;
            boolean flag;
            do {
                key = UUID.randomUUID().toString();
                flag = !submitMap.putIfAbsentBoolean(key, sendCommandStatistics);
            } while (flag);
            sendDB.commit();
            return key;
        } catch (Exception e) {
            sendDB.rollback();
            throw new RuntimeException(e);
        }
    }

    public SendCommandStatistics getSendCommandStatistics(String key) {
        return getSubmitMap(sendDB).get(key);
    }

    public synchronized void addSendQueue(String key) {
        sendQueueLock.lock();
        try {
            BTreeMap<Long, String> sendQueue = getSendQueue(sendDB);
            Map.Entry<Long, String> lastEntry = sendQueue.lastEntry();
            if (null == lastEntry) {
                sendQueue.put(0L, key);
            } else {
                sendQueue.put(lastEntry.getKey() + 1, key);
            }
            sendDB.commit();
            sendQueueCondition.signal();
        } catch (Exception e) {
            sendDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        } finally {
            sendQueueLock.unlock();
        }
    }

    public String takeFromSendQueue() {
        sendQueueLock.lock();
        try {
            BTreeMap<Long, String> sendQueue = getSendQueue(sendDB);
            Map.Entry<Long, String> firstEntry = sendQueue.firstEntry();
            while (null == firstEntry) {
                sendQueueCondition.await();
                firstEntry = sendQueue.firstEntry();
            }
            sendQueue.remove(firstEntry.getKey());
            sendDB.commit();
            return firstEntry.getValue();
        } catch (Exception e) {
            sendDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        } finally {
            sendQueueLock.unlock();
        }
    }

    public void addDelayQueue(String key) {
        try {
            BTreeMap<Long, String> delayQueue = getSendDelayQueue();

            Map.Entry<Long, String> lastEntry = delayQueue.lastEntry();
            if (null == lastEntry) {
                delayQueue.put(0L, key);
            } else {
                delayQueue.put(lastEntry.getKey() + 1, key);
            }
            sendDB.commit();
        } catch (Exception e) {
            sendDB.rollback();
            log.error("{}", e);
        }
    }

    private void transFromDelayToSendQueue() {
        try {
            BTreeMap<Long, String> delayQueue = getSendDelayQueue();
            Map.Entry<Long, String> entry = delayQueue.firstEntry();
            if (null == entry) {
                return;
            }
            delayQueue.remove(entry.getKey());
            addSendQueue(entry.getValue());
            log.info("trans {} from delay to send queue", entry.getValue());
            sendDB.commit();
        } catch (Exception e) {
            sendDB.rollback();
            throw new RuntimeException(e);
        }
    }


    private void gc() {
        try {
            NavigableSet<String> gcSet = getGCSet(sendDB);
            HTreeMap<String, SendCommandStatistics> submitMap = getSubmitMap(sendDB);

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
            sendDB.commit();
        } catch (Exception e) {
            sendDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    public void updateSendCommandStatics(String key, SendCommandStatistics sendCommandStatistics) {
        try {
            getSubmitMap(sendDB).put(key, sendCommandStatistics);
            sendDB.commit();
        } catch (Exception e) {
            sendDB.rollback();
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    public void addGCSet(String key) {
        try {
            NavigableSet<String> gcSet = getGCSet(sendDB);
            gcSet.add(key);
            sendDB.commit();
        } catch (Exception e) {
            sendDB.rollback();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private HTreeMap<String, SendCommandStatistics> getSubmitMap(DB mapDB) {
        return mapDB.hashMap(COMMAND_SUBMIT_MAP)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }


    private BTreeMap<Long, String> getSendQueue(DB mapDB) {
        return mapDB.treeMap(COMMAND_SEND_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();
    }

    @SuppressWarnings("unchecked")
    private BTreeMap<Long, String> getSendDelayQueue() {
        return sendDB.treeMap(COMMAND_DELAY_QUEUE)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    private NavigableSet<String> getGCSet(DB mapDB) {
        return mapDB.treeSet(COMMAND_GC_QUEUE)
                .serializer(Serializer.STRING)
                .createOrOpen();
    }
}
