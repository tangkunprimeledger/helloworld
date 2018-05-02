package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.higgs.trust.consensus.common.TraceUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.SendStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.SendCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.sleuth.Span;

import java.util.concurrent.*;

/**
 * @author cwy
 */
@Slf4j
public class ConsensusContext {
    private SendStorage sendStorage;
    private ReceiveStorage receiveStorage;
    private ValidConsensus validConsensus;
    private P2pConsensusClient p2pConsensusClient;
    private Integer applyThreshold;
    private Integer totalNodeNum;
    private ExecutorService sendExecutorService;

    private ConsensusContext(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, String baseDir, Integer fautNodeNum, Integer totalNodeNum) {
        this.validConsensus = validConsensus;
        sendStorage = SendStorage.createFileStorage(baseDir.concat("sendDB"));
        receiveStorage = ReceiveStorage.createFileStorage(baseDir.concat("receiveDB"));
        this.p2pConsensusClient = p2pConsensusClient;
        this.applyThreshold = 2 * fautNodeNum + 1;
        this.totalNodeNum = totalNodeNum;
        initExecutor();
    }

    private ConsensusContext(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, Integer fautNodeNum, Integer totalNodeNum) {
        this.validConsensus = validConsensus;
        sendStorage = SendStorage.createMemoryStorage();
        receiveStorage = ReceiveStorage.createMemoryStorage();
        this.p2pConsensusClient = p2pConsensusClient;
        this.applyThreshold = 2 * fautNodeNum + 1;
        this.totalNodeNum = totalNodeNum;
        initExecutor();
    }

    public static ConsensusContext create(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, String baseDir, Integer fautNodeNum, Integer totalNodeNum) {
        return new ConsensusContext(validConsensus, p2pConsensusClient, baseDir, fautNodeNum, totalNodeNum);
    }

    public static ConsensusContext createInMemory(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, Integer fautNodeNum, Integer totalNodeNum) {
        return new ConsensusContext(validConsensus, p2pConsensusClient, fautNodeNum, totalNodeNum);
    }

    private void initExecutor() {
        sendExecutorService = new ThreadPoolExecutor(1, 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("sending command thread");
            thread.setDaemon(true);
            return thread;
        });

        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::send);

        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::apply);
    }

    public void submit(ValidCommandWrap validCommandWrap) {
        sendStorage.openTx();
        try {
            String key = sendStorage.submit(validCommandWrap);
            sendStorage.addSendQueue(key);
            sendStorage.commit();
        }catch (Throwable e){
            sendStorage.rollBack();
            throw new RuntimeException(e);
        }finally {
            sendStorage.closeTx();
        }
    }

    /**
     * receive the commandWrap
     * @param validCommandWrap validCommandWrap
     */
    public synchronized void receive(ValidCommandWrap validCommandWrap) {
        receiveStorage.openTx();
        try{
            String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
            ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.add(key, validCommandWrap);

            if (receiveCommandStatistics.isClosed()) {
                log.info("receiveCommandStatistics {} from node {} is closed", receiveCommandStatistics, validCommandWrap.getFromNodeName());
                if (receiveCommandStatistics.getFromNodeNameSet().size() == totalNodeNum) {
                    log.info("add receiveCommandStatistics {} to gc set", receiveCommandStatistics);
                    receiveStorage.addGCSet(key);
                }
            } else if (receiveCommandStatistics.isApply()) {
                log.info("receiveCommandStatistics {} has applied", receiveCommandStatistics);
            } else if (receiveCommandStatistics.getFromNodeNameSet().size() >= applyThreshold) {
                receiveStorage.addApplyQueue(key);
                receiveCommandStatistics.apply();
                receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
                log.info("from node set size {} >= threshold {}, trigger apply", receiveCommandStatistics.getFromNodeNameSet().size(), applyThreshold);
            }
            receiveStorage.commit();
        }catch (Throwable e){
            log.error("{}", e);
            receiveStorage.rollBack();
            throw new RuntimeException("receive exception");
        }finally {
            receiveStorage.closeTx();
        }


    }

    /**
     * send the validCommandWrap to server
     */
    private void send() {
        while (true) {
            sendStorage.openTx();
            String rootKey = null;
            try {
                String key = sendStorage.takeFromSendQueue();
                rootKey = key;
                if (null == key) {
                    log.warn("key is null");
                    continue;
                }
                SendCommandStatistics sendCommandStatistics = sendStorage.getSendCommandStatistics(key);

                if (null == sendCommandStatistics) {
                    log.warn("key {} sendCommandStatistics is null", key);
                    continue;
                }

                if (sendCommandStatistics.isSend()) {
                    log.warn("sendCommandStatistics {} has been send", sendCommandStatistics);
                    continue;
                }

                log.info("schedule send sendCommandStatistics {}", sendCommandStatistics);

                // set the countDownLatch
                CountDownLatch countDownLatch = new CountDownLatch(sendCommandStatistics.getSendNodeNames().size());

                for (String nodeName : sendCommandStatistics.getSendNodeNames()) {
                    if (log.isTraceEnabled()) {
                        log.trace("nodeName is {}", nodeName);
                    }

                    if (sendCommandStatistics.getAckNodeNames().contains(nodeName)) {
                        countDownLatch.countDown();
                        continue;
                    }
                    sendExecutorService.submit(() -> {
                        ValidCommandWrap validCommandWrap = sendCommandStatistics.getValidCommandWrap();
                        try {
                            String result = p2pConsensusClient.receiveCommand(nodeName, validCommandWrap);
                            if (StringUtils.equals("SUCCESS", result)) {
                                sendCommandStatistics.addAckNodeName(nodeName);
                                sendStorage.updateSendCommandStatics(key, sendCommandStatistics);
                            }
                            log.info("result is {}", result);
                        } catch (Exception e) {
                            log.error("{}", e);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
                countDownLatch.await();
                //gc
                if (sendCommandStatistics.getAckNodeNames().size() == sendCommandStatistics.getSendNodeNames().size()) {
                    sendStorage.addGCSet(key);
                    sendCommandStatistics.setSend();
                    sendStorage.updateSendCommandStatics(key, sendCommandStatistics);
                } else {
                    sendStorage.addDelayQueue(key);
                }
                sendStorage.removeFromSendQueue(key);
                sendStorage.commit();
            } catch (Throwable e) {
                log.error("{}", e);
                if (null != rootKey) {
                    try{
                        sendStorage.addDelayQueue(rootKey);
                        sendStorage.commit();
                    }catch (Throwable throwable){
                        log.error("{}", throwable);
                        sendStorage.rollBack();
                    }
                }
            } finally {
                sendStorage.closeTx();
            }
        }
    }

    private void apply() {
        while (true) {
            receiveStorage.openTx();
            String key = null;
            Span span = null;
            try {
                key = receiveStorage.takeFromApplyQueue();
                if (null == key) {
                    log.warn("key is null");
                    continue;
                }
                ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);

                if (null == receiveCommandStatistics) {
                    log.warn("receiveCommandStatistics is null, key is {}", key);
                    continue;
                }

                span = TraceUtils.createSpan(receiveCommandStatistics.getTraceId());

                log.info("schedule apply receiveCommandStatistics {}", receiveCommandStatistics);

                if (receiveCommandStatistics.isClosed()) {
                    log.warn("receiveCommandStatistics {} is closed, key is {}", receiveCommandStatistics, key);
                    continue;
                }

                validConsensus.apply(receiveCommandStatistics);
                if (receiveCommandStatistics.isClosed()) {
                    if (receiveCommandStatistics.getFromNodeNameSet().size() == totalNodeNum) {
                        log.info("gather all the nodes {} command", receiveCommandStatistics.getFromNodeNameSet());
                        receiveStorage.addGCSet(key);
                    }
                    receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
                } else {
                    log.info("apply {} not close, add key {} to delay queue", receiveCommandStatistics.getValidCommand(), key);
                    receiveStorage.addDelayQueue(key);
                }
                receiveStorage.commit();
            } catch (Throwable e) {
                log.error("apply log failed! {}", e);
                if (null != key) {
                    log.info("apply exception, add key {} to delay queue", key);
                    try{
                        receiveStorage.addDelayQueue(key);
                        receiveStorage.commit();
                    }catch (Throwable throwable){
                        log.error("{}", e);
                        receiveStorage.rollBack();
                    }
                }
            } finally {
                receiveStorage.closeTx();
                TraceUtils.closeSpan(span);
            }
        }
    }
}
