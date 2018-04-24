package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.SendStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.SendCommandStatistics;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cwy
 */
@Slf4j public class ConsensusContext {
    private SendStorage sendStorage;
    private ReceiveStorage receiveStorage;
    private ValidConsensus validConsensus;
    private P2pConsensusClient p2pConsensusClient;

    public ConsensusContext(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, String sendDBDir, String receiveDBDir) {
        this.validConsensus = validConsensus;
        sendStorage = new SendStorage(sendDBDir);
        receiveStorage = new ReceiveStorage(receiveDBDir);
        this.p2pConsensusClient = p2pConsensusClient;
        initExecutor();
    }

    private void initExecutor() {
        new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("valid command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(() -> {
            send();
        });

        new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("valid command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(() -> {
            apply();
        });
    }

    public void submit(ValidCommandWrap validCommandWrap) {
        String key = sendStorage.submit(validCommandWrap);
        sendStorage.addSendQueue(key);
    }

    /**
     * receive the commandWrap
     *
     * @param validCommandWrap
     */
    public void receive(ValidCommandWrap validCommandWrap) {
        String key = receiveStorage.add(validCommandWrap);
        receiveStorage.addApplyQueue(key);
    }

    public void send() {
        for (; ; ) {
            String key = null;
            try {
                key = sendStorage.takeFromSendQueue();
                if (null == key) {
                    log.warn("key is null");
                    return;
                }
                SendCommandStatistics sendCommandStatistics = sendStorage.getSendCommandStatistics(key);
                log.info("schedule send sendCommandStatistics {}", sendCommandStatistics);
                //send
                sendCommandStatistics.getValidCommandWrap().getToNodeNames().forEach((nodeName)->{
                    try{
                        if(log.isTraceEnabled()){
                            log.trace("nodeName is {}", nodeName);
                        }
                        p2pConsensusClient.receiveCommand(nodeName,sendCommandStatistics.getValidCommandWrap());
                    }catch (Exception e){
                        log.error("{}", e);
                    }
                });
                //gc
                sendStorage.addGCSet(key);
            } catch (Exception e) {
                log.error("{}", e);
                if (null != key) {
                    sendStorage.addSendQueue(key);
                }
            }
        }
    }

    public void apply() {
        for (; ; ) {
            String key = null;
            try {
                key = receiveStorage.takeFromApplyQueue();
                if (null == key) {
                    log.warn("key is null");
                    return;
                }
                ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);

                log.info("schedule apply receiveCommandStatistics {}", receiveCommandStatistics);
                if (null == receiveCommandStatistics || receiveCommandStatistics.isClosed()) {
                    log.warn("receiveCommandStatistics {} is invalid, key is {}", receiveCommandStatistics, key);
                    return;
                }
                validConsensus.apply(receiveCommandStatistics);
                if (receiveCommandStatistics.isClosed()) {
                    if (receiveCommandStatistics.getFromNodeNameSet().size() >= 0) {
                        receiveStorage.addGCSet(key);
                    }
                    receiveStorage.updateReceiveCommandStatistics(key, receiveCommandStatistics);
                } else {
                    log.info("apply not close, add key {} to delay queue", key);
                    receiveStorage.addDelayQueue(key);
                }
            } catch (Exception e) {
                log.error("apply log failed!", e);
                if (null != key) {
                    log.info("apply exception, add key {} to delay queue", key);
                    receiveStorage.addDelayQueue(key);
                }
            }
        }
    }
}
