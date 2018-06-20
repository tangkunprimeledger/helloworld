package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import com.higgs.trust.consensus.p2pvalid.dao.po.*;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component @Slf4j public class SendService {

    private static final Integer COMMAND_QUEUED_SEND = 0;
    private static final Integer COMMAND_QUEUED_GC = 1;

    private static final Integer SEND_NODE_WAIT_SEND = 0;
    private static final Integer SEND_NODE_ACK = 1;

    @Autowired private SendCommandDao sendCommandDao;

    @Autowired private SendNodeDao sendNodeDao;

    @Autowired private QueuedSendDao queuedSendDao;

    @Autowired private QueuedSendDelayDao queuedSendDelayDao;

    @Autowired private QueuedSendGcDao queuedSendGcDao;

    @Autowired private TransactionTemplate txRequired;

    @Autowired private TransactionTemplate txRequiresNew;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private NodeState nodeState;

    @Value("${p2p.send.gc.interval:6000}") private Long gcInterval;

    @Value("${p2p.send.increase.delay.interval:3000}") private Long delayIncreaseInterval;

    @Value("${p2p.send.delay.max:7200000}") private Long delayDelayMax;

    /**
     * send lock
     */
    private final ReentrantLock sendLock = new ReentrantLock(true);

    /**
     * send condition
     */
    private final Condition sendCondition = sendLock.newCondition();

    /**
     * delay send lock
     */
    private final ReentrantLock delaySendLock = new ReentrantLock(true);

    /**
     * delay send condition
     */
    private final Condition delaySendCondition = delaySendLock.newCondition();

    private ExecutorService sendExecutorService;

    @PostConstruct public void initThreadPool() {
        new ThreadPoolExecutor(1, 1, 600 * 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::send);

        new ThreadPoolExecutor(1, 1, 600 * 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send delay thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::sendDelay);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, gcInterval, gcInterval, TimeUnit.MILLISECONDS);

        sendExecutorService =
            new ThreadPoolExecutor(20, 80, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
                Thread thread = new Thread(r);
                thread.setName("command send thread executor");
                thread.setDaemon(true);
                return thread;
            });

    }

    /**
     * @param validCommand
     * @throws Exception
     */
    public void submit(ValidCommand<?> validCommand) {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new RuntimeException(String.format("the node state is not running, please try again latter"));
        }
        SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(validCommand.getMessageDigestHash());
        if (null != sendCommand) {
            log.warn("duplicate command {}", validCommand);
            return;
        }

        txRequiresNew.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                SendCommandPO sendCommand = new SendCommandPO();
                sendCommand.setAckNodeNum(0);
                sendCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                sendCommand.setNodeName(clusterInfo.nodeName());
                sendCommand
                    .setCommandSign(SignUtils.sign(validCommand.getMessageDigestHash(), clusterInfo.privateKey()));
                sendCommand.setMessageDigest(validCommand.getMessageDigestHash());
                sendCommand.setStatus(COMMAND_QUEUED_SEND);
                sendCommand.setRetrySendNum(0);
                sendCommand.setValidCommand(JSON.toJSONString(validCommand));
                sendCommand.setCommandClass(validCommand.getClass().getSimpleName());
                try {
                    sendCommandDao.add(sendCommand);
                } catch (DuplicateKeyException e) {
                    // do no thing when idempotent
                    return;
                }

                clusterInfo.clusterNodeNames().forEach((toNodeName) -> {
                    SendNodePO sendNodePO = new SendNodePO();
                    sendNodePO.setMessageDigest(validCommand.getMessageDigestHash());
                    sendNodePO.setToNodeName(toNodeName);
                    sendNodePO.setStatus(SEND_NODE_WAIT_SEND);
                    sendNodeDao.add(sendNodePO);
                });
                queuedSend(sendCommand);
            }
        });

        //signal wait
        sendLock.lock();
        try {
            sendCondition.signal();
            log.info("signal the send thread");
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * send the validCommandWrap to server, please guaranteed thread safe
     */
    private void send() {
        while (true) {
            if (!nodeState.isState(NodeStateEnum.Running)) {
                return;
            }
            try {
                List<QueuedSendPO> queuedSendList = takeSendList();
                queuedSendList.forEach((queuedSend) -> {
                    Span span = TraceUtils.createSpan();
                    try {
                        SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(queuedSend.getMessageDigest());
                        if (null == sendCommand) {
                            escapeQueuedSend(queuedSend);
                            return;
                        }

                        if (sendCommand.getStatus().equals(COMMAND_QUEUED_GC)) {
                            log.warn("command is queued gc : {}", sendCommand);
                            return;
                        }

                        sendCommand(sendCommand);

                        //delete with transactions
                        txRequired.execute(new TransactionCallbackWithoutResult() {
                            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                                queuedSendDao.deleteByMessageDigest(queuedSend.getMessageDigest());
                                checkGc(sendCommand);
                            }
                        });

                        delaySendLock.lock();
                        try {
                            delaySendCondition.signal();
                        } catch (Exception e) {
                            log.error("{}", e);
                        } finally {
                            delaySendLock.unlock();
                        }

                    } finally {
                        TraceUtils.closeSpan(span);
                    }
                });
            } catch (Throwable throwable) {
                log.error("p2p send process failed", throwable);
            }
        }
    }

    /**
     * send delay command
     */
    private void sendDelay() {
        while (true) {
            if (!nodeState.isState(NodeStateEnum.Running)) {
                return;
            }
            try {
                List<QueuedSendDelayPO> queuedSendDelayList = takeDelaySendList();
                queuedSendDelayList.forEach((queuedSendDelay) -> {
                    Span span = TraceUtils.createSpan();
                    try {
                        SendCommandPO sendCommand =
                            sendCommandDao.queryByMessageDigest(queuedSendDelay.getMessageDigest());
                        if (null == sendCommand) {
                            escapeQueuedDelaySend(queuedSendDelay);
                            return;
                        }
                        sendCommand(sendCommand);

                        //delete with transactions
                        txRequired.execute(new TransactionCallbackWithoutResult() {
                            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                                queuedSendDelayDao.deleteByMessageDigest(queuedSendDelay.getMessageDigest());
                                checkGc(sendCommand);
                            }
                        });

                        delaySendLock.lock();
                        try {
                            delaySendCondition.signal();
                        } catch (Exception e) {
                            log.error("{}", e);
                        } finally {
                            delaySendLock.unlock();
                        }

                    } finally {
                        TraceUtils.closeSpan(span);
                    }
                });

            } catch (Throwable throwable) {
                log.error("p2p send delay process failed", throwable);
            }
        }
    }

    /**
     * @return List<QueuedSendDelayPO>
     */
    private List<QueuedSendDelayPO> takeDelaySendList() {
        delaySendLock.lock();
        try {
            List<QueuedSendDelayPO> queuedSendDelayList =
                queuedSendDelayDao.queryListBySendTime(System.currentTimeMillis());
            while (CollectionUtils.isEmpty(queuedSendDelayList)) {
                delaySendCondition.await(10, TimeUnit.SECONDS);
                queuedSendDelayList = queuedSendDelayDao.queryListBySendTime(System.currentTimeMillis());
            }
            return queuedSendDelayList;
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            delaySendLock.unlock();
        }
        return new ArrayList<>();
    }

    /**
     * send command
     *
     * @param sendCommand
     */
    private void sendCommand(SendCommandPO sendCommand) {
        log.info("send command {}", sendCommand);
        List<SendNodePO> sendNodeList =
            sendNodeDao.queryByDigestAndStatus(sendCommand.getMessageDigest(), SEND_NODE_WAIT_SEND);
        CountDownLatch countDownLatch = new CountDownLatch(sendNodeList.size());

        sendNodeList.forEach((sendNode) -> {
            sendExecutorService.submit(() -> {
                try {

                    ValidCommandWrap validCommandWrap = new ValidCommandWrap();
                    validCommandWrap.setCommandClass(sendCommand.getValidCommand().getClass());
                    validCommandWrap.setFromNode(sendCommand.getNodeName());
                    validCommandWrap.setSign(sendCommand.getCommandSign());
                    validCommandWrap.setValidCommand((ValidCommand<?>)JSON.parse(sendCommand.getValidCommand()));
                    ValidResponseWrap<? extends ResponseCommand> sendValidResponse =
                        p2pConsensusClient.send(sendNode.getToNodeName(), validCommandWrap);

                    if (sendValidResponse.isSucess()) {
                        int count = sendNodeDao
                            .transStatus(sendNode.getMessageDigest(), sendNode.getToNodeName(), SEND_NODE_WAIT_SEND,
                                SEND_NODE_ACK);
                        if (count != 1) {
                            throw new RuntimeException("trans send node status failed when apply! count: " + count);
                        }
                        log.info("send command to node success {} ", sendNode);
                    } else {
                        log.error("send command to node failed {}, error {} ", sendNode,
                            sendValidResponse.getMessage());
                    }

                } catch (Throwable throwable) {
                    log.error("send to node error {}", sendNode, throwable);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("send count down latch is interrupted", e);
        }

        //count ack num avoid send executors concurrence
        int ackNodeNum = sendNodeDao.countByDigestAndStatus(sendCommand.getMessageDigest(), SEND_NODE_ACK);

        //just count without lock because of send function executes serially
        //TODO 如果未来并发执行，锁sendCommand然后再count
        sendCommand.setAckNodeNum(ackNodeNum);
    }

    public void checkGc(SendCommandPO sendCommand) {
        sendCommandDao.updateAckNodeNum(sendCommand.getMessageDigest(), sendCommand.getAckNodeNum());

        if (sendCommand.getAckNodeNum() >= sendCommand.getGcThreshold()) {
            queuedGc(sendCommand);
            log.info("ack node num >= gc threshold, add command to gc {}", sendCommand);
        } else {
            sendCommandDao.increaseRetrySendNum(sendCommand.getMessageDigest());

            Long delayTime = (sendCommand.getRetrySendNum() + 1) * delayIncreaseInterval;
            delayTime = Math.min(delayTime, delayDelayMax);
            queuedDelay(sendCommand, delayTime);

            log.info("ack node num {} < gc threshold {}, add to delay send queue {}", sendCommand.getAckNodeNum(),
                sendCommand.getGcThreshold(), sendCommand);
        }
    }

    /**
     * send gc, please guaranteed thread safe
     */
    public void gc() {
        List<QueuedSendGcPO> queuedSendGcList = queuedSendGcDao.queryGcList(System.currentTimeMillis());
        if (CollectionUtils.isEmpty(queuedSendGcList)) {
            return;
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedSendGcList.forEach((queuedSendGc) -> {
                    deleteMessageDigestList.add(queuedSendGc.getMessageDigest());
                });

                if (!CollectionUtils.isEmpty(deleteMessageDigestList)) {
                    //delete send command
                    sendCommandDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete queued gc
                    queuedSendGcDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete send node
                    sendNodeDao.deleteByMessageDigestList(deleteMessageDigestList);
                }
                log.info("send gc {}", deleteMessageDigestList);
            }
        });
    }

    /**
     * take send list
     *
     * @return List<QueuedSendPO>
     */
    private List<QueuedSendPO> takeSendList() {
        sendLock.lock();
        try {
            List<QueuedSendPO> queuedSendList = queuedSendDao.querySendList();
            while (CollectionUtils.isEmpty(queuedSendList)) {
                sendCondition.await(20, TimeUnit.SECONDS);
                queuedSendList = queuedSendDao.querySendList();
            }
            return queuedSendList;
        } catch (Exception e) {
            log.error("takeSendList take send list error : {}", e);
        } finally {
            sendLock.unlock();
        }
        return new ArrayList<>();
    }

    private void escapeQueuedSend(QueuedSendPO queuedSend) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape send command is null of messageDigest {}", queuedSend);
                queuedSendDao.deleteByMessageDigest(queuedSend.getMessageDigest());
            }
        });
    }

    private void escapeQueuedDelaySend(QueuedSendDelayPO queuedSendDelay) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape send delay command is null of messageDigest {}", queuedSendDelay);
                queuedSendDelayDao.deleteByMessageDigest(queuedSendDelay.getMessageDigest());
            }
        });
    }

    private void queuedSend(SendCommandPO sendCommand) {
        QueuedSendPO queuedSendPO = queuedSendDao.queryByMessageDigest(sendCommand.getMessageDigest());
        if (null != queuedSendPO) {
            return;
        }
        queuedSendPO = new QueuedSendPO();
        queuedSendPO.setMessageDigest(sendCommand.getMessageDigest());
        queuedSendDao.add(queuedSendPO);
    }

    private void queuedGc(SendCommandPO sendCommand) {
        QueuedSendGcPO queuedSendGcPO = new QueuedSendGcPO();
        queuedSendGcPO.setMessageDigest(sendCommand.getMessageDigest());
        //TODO 配置化，目前改成了10分钟
        queuedSendGcPO.setGcTime(System.currentTimeMillis() + 6000L);
        queuedSendGcDao.add(queuedSendGcPO);
        int count = sendCommandDao.transStatus(sendCommand.getMessageDigest(), COMMAND_QUEUED_SEND, COMMAND_QUEUED_GC);
        if (count != 1) {
            throw new RuntimeException("trans send command status failed when mark gc! count: " + count);
        }
    }

    private void queuedDelay(SendCommandPO sendCommand, Long delayTime) {
        QueuedSendDelayPO queuedSendDelay = new QueuedSendDelayPO();
        queuedSendDelay.setMessageDigest(sendCommand.getMessageDigest());
        //TODO 配置化
        queuedSendDelay.setSendTime(System.currentTimeMillis() + delayTime);
        queuedSendDelayDao.add(queuedSendDelay);
    }

}
