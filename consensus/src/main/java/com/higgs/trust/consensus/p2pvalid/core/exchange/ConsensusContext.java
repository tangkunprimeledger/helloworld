package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.higgs.trust.consensus.common.TraceUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
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
        sendExecutorService = new ThreadPoolExecutor(5, 10, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("sending command thread");
            thread.setDaemon(true);
            return thread;
        });

//        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
//            Thread thread = new Thread(r);
//            thread.setName("command send thread");
//            thread.setDaemon(true);
//            return thread;
//        }).execute(this::send);

        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::apply);
    }

    public void submit(ValidCommandWrap validCommandWrap) {
        receive(validCommandWrap);
//        sendStorage.openTx();
//        try {
//            String key = sendStorage.submit(validCommandWrap);
//            sendStorage.addSendQueue(key);
//            sendStorage.commit();
//        }catch (Throwable e){
//            sendStorage.rollBack();
//            throw new RuntimeException(e);
//        }finally {
//            sendStorage.closeTx();
//        }
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
            String key = sendStorage.takeFromSendQueue();
            try {
                if (null == key) {
                    log.warn("key is null");
                    sendStorage.removeFromSendQueue();
                    continue;
                }
                final SendCommandStatistics sendCommandStatistics;

                sendStorage.openTx();
                try{
                    sendCommandStatistics = sendStorage.getSendCommandStatistics(key);
                }finally {
                    sendStorage.closeTx();
                }

                if (null == sendCommandStatistics) {
                    log.warn("key {} sendCommandStatistics is null", key);
                    sendStorage.removeFromSendQueue();
                    continue;
                }

                if (sendCommandStatistics.isSend()) {
                    log.warn("sendCommandStatistics {} has been send", sendCommandStatistics);
                    sendStorage.removeFromSendQueue();
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
                            Long start = System.currentTimeMillis();
                            String result = p2pConsensusClient.receiveCommand(nodeName, validCommandWrap);
                            if (StringUtils.equals("SUCCESS", result)) {
                                sendCommandStatistics.addAckNodeName(nodeName);
                            }
                            Long end = System.currentTimeMillis();
                            ValidCommand<?> validCommand = sendCommandStatistics.getValidCommandWrap().getValidCommand();
                            log.info("p2p consensus {} from {} send to {} result is {} , start time {}, end time {}, duration {}, command is {}",
                                    validCommand.getClass(), sendCommandStatistics.getValidCommandWrap().getFromNodeName(), nodeName,
                                    result, start, end, end - start, validCommand);
                        } catch (Exception e) {
                            log.error("{}", e);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
                countDownLatch.await();
                sendStorage.openTx();
                try{
                    //gc
                    if (sendCommandStatistics.getAckNodeNames().size() == sendCommandStatistics.getSendNodeNames().size()) {
                        sendCommandStatistics.setSend();
                        sendStorage.addGCSet(key);
                    } else {
                        sendStorage.addDelayQueue(key);
                    }
                    sendStorage.updateSendCommandStatics(key, sendCommandStatistics);
                    sendStorage.removeFromSendQueue();
                    sendStorage.commit();
                }finally {
                    sendStorage.closeTx();
                }
            } catch (Throwable e) {
                log.error("{}", e);
                sendStorage.openTx();
                try{
                    if(null != key){
                        sendStorage.addDelayQueue(key);
                    }
                    sendStorage.removeFromSendQueue();
                    sendStorage.commit();
                }finally {
                    sendStorage.closeTx();
                }

            }
        }
    }

    private void apply() {
        while (true) {
            String key = null;
            Span span = null;
            try {
                key = receiveStorage.getFirstFromApplyQueue();
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
                    receiveStorage.deleteFirstFromApplyQueue();
                    continue;
                }

                validConsensus.apply(receiveCommandStatistics);

                if (receiveCommandStatistics.isClosed()) {
                    receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
                    if (receiveCommandStatistics.getFromNodeNameSet().size() == totalNodeNum) {
                        log.info("gather all the nodes {} command", receiveCommandStatistics.getFromNodeNameSet());
                        receiveStorage.addGCSet(key);
                    }
                } else {
                    log.info("apply {} not close, add key {} to delay queue", receiveCommandStatistics.getValidCommand(), key);
                    receiveStorage.addDelayQueue(key);
                }

                receiveStorage.deleteFirstFromApplyQueue();
                receiveStorage.openTx();
                try {
                    receiveStorage.commit();
                } finally {
                    receiveStorage.closeTx();
                }
            } catch (Throwable e) {
                log.error("apply log failed! {}", e);
                if (null != key) {
                    log.info("apply exception, add key {} to delay queue", key);
                    receiveStorage.openTx();
                    try{
                        if(null != key){
                            receiveStorage.addDelayQueue(key);
                        }
                        receiveStorage.deleteFirstFromApplyQueue();
                        receiveStorage.commit();
                    } finally {
                        receiveStorage.closeTx();
                    }
                }
            } finally {
                TraceUtils.closeSpan(span);
            }
        }
    }
}
