package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.SendStorage;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.SendCommandStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    public static ConsensusContext create(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, String baseDir, Integer fautNodeNum, Integer totalNodeNum){
        return  new ConsensusContext(validConsensus, p2pConsensusClient, baseDir, fautNodeNum, totalNodeNum);
    }

    public static ConsensusContext createInMemory(ValidConsensus validConsensus, P2pConsensusClient p2pConsensusClient, Integer fautNodeNum, Integer totalNodeNum){
        return  new ConsensusContext(validConsensus, p2pConsensusClient, fautNodeNum, totalNodeNum);
    }

    private void initExecutor() {
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
        String key = sendStorage.submit(validCommandWrap);
        sendStorage.addSendQueue(key);
    }

    /**
     * receive the commandWrap
     *
     * @param validCommandWrap
     */
    public void receive(ValidCommandWrap validCommandWrap) {
        String key = validCommandWrap.getCommandClass().getName().concat("_").concat(validCommandWrap.getMessageDigest());
        ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.add(key, validCommandWrap);

        if(receiveCommandStatistics.isClosed()){
            log.info("receiveCommandStatistics {} from node {} is apply and closed", receiveCommandStatistics, validCommandWrap.getFromNodeName());
            if(receiveCommandStatistics.getFromNodeNameSet().size() == totalNodeNum){
                log.info("add receiveCommandStatistics {} to gc set", receiveCommandStatistics);
                receiveStorage.addGCSet(key);
            }
        }else if (receiveCommandStatistics.getFromNodeNameSet().size() >= applyThreshold) {
            log.info("from node set size {} > threshold {}, trigger apply", receiveCommandStatistics.getFromNodeNameSet().size(), applyThreshold);
            receiveStorage.addApplyQueue(key);
        }
    }

    private void send() {
        while (true) {
            String key = null;
            try {
                key = sendStorage.takeFromSendQueue();
                if (null == key) {
                    log.warn("key is null");
                    continue;
                }
                SendCommandStatistics sendCommandStatistics = sendStorage.getSendCommandStatistics(key);
                log.info("schedule send sendCommandStatistics {}", sendCommandStatistics);
                for (String nodeName : sendCommandStatistics.getSendNodeNames()) {
                    try {
                        if (log.isTraceEnabled()) {
                            log.trace("nodeName is {}", nodeName);
                        }

                        if (sendCommandStatistics.getAckNodeNames().contains(nodeName)) {
                            continue;
                        }

                        String result = p2pConsensusClient.receiveCommand(nodeName, sendCommandStatistics.getValidCommandWrap());
                        if (StringUtils.equals("SUCCESS", result)) {
                            sendCommandStatistics.addAckNodeName(nodeName);
                            sendStorage.updateSendCommandStatics(key, sendCommandStatistics);
                        }
                        log.info("result is {}", result);
                    } catch (Exception e) {
                        log.error("{}", e);
                    }
                }
                //gc
                if (sendCommandStatistics.getAckNodeNames().size() == sendCommandStatistics.getSendNodeNames().size()) {
                    sendStorage.addGCSet(key);
                }else{
                    sendStorage.addDelayQueue(key);
                }
            } catch (Exception e) {
                log.error("{}", e);
                if (null != key) {
                    sendStorage.addDelayQueue(key);
                }
            }
        }
    }

    private void apply() {
        while (true) {
            String key = null;
            try {
                key = receiveStorage.takeFromApplyQueue();
                if (null == key) {
                    log.warn("key is null");
                    continue;
                }
                ReceiveCommandStatistics receiveCommandStatistics = receiveStorage.getReceiveCommandStatistics(key);

                log.info("schedule apply receiveCommandStatistics {}", receiveCommandStatistics);
                if (null == receiveCommandStatistics || receiveCommandStatistics.isClosed()) {
                    log.warn("receiveCommandStatistics {} is invalid, key is {}", receiveCommandStatistics, key);
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
            } catch (Exception e) {
                log.error("apply log failed! {}", e);
                if (null != key) {
                    log.info("apply exception, add key {} to delay queue", key);
                    receiveStorage.addDelayQueue(key);
                }
            }
        }
    }
}
