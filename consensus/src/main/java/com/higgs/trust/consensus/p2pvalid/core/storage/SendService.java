package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import com.higgs.trust.consensus.p2pvalid.dao.po.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Component
@Slf4j
public class SendService {

    private static final Integer COMMAND_QUEUED_SEND = 0;
    private static final Integer COMMAND_QUEUED_GC = 1;

    private static final Integer SEND_NODE_WAIT_SEND = 0;
    private static final Integer SEND_NODE_ACK = 1;

    @Autowired
    private SendCommandDao sendCommandDao;

    @Autowired
    private SendNodeDao sendNodeDao;

    @Autowired
    private QueuedSendDao queuedSendDao;

    @Autowired
    private QueuedSendDelayDao queuedSendDelayDao;

    @Autowired
    private QueuedSendGcDao queuedSendGcDao;

    @Autowired
    private TransactionTemplate txRequired;

    @Autowired
    private ClusterInfo clusterInfo;

    @Autowired
    private ReceiveService receiveService;

    @Autowired
    private P2pConsensusClient p2pConsensusClient;

    @Value("${p2p.send.delay.interval:2}")
    private Long transDelayInterval;

    @Value("${p2p.send.gc.interval:10}")
    private Long gcInterval;

    /**
     * send lock
     */
    private final ReentrantLock sendLock = new ReentrantLock(true);

    /**
     * send condition
     */
    private final Condition sendCondition = sendLock.newCondition();


    private ExecutorService sendExcutorService;


    @PostConstruct
    public void initThreadPool() {
        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::send);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send trans thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::transFromDelayToSend, transDelayInterval, transDelayInterval, TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, gcInterval, gcInterval, TimeUnit.SECONDS);

        sendExcutorService = new ThreadPoolExecutor(4, 10, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send thread");
            thread.setDaemon(true);
            return thread;
        });

    }

    /**
     * @param validCommand
     * @throws Exception
     */
    public void submit(ValidCommand<?> validCommand) {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(validCommand.getMessageDigestHash());
                if (null != sendCommand) {
                    log.warn("duplicate command {}", validCommand);
                }else{
                    sendCommand = new SendCommandPO();
                    sendCommand.setAckNodeNum(0);
                    sendCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                    sendCommand.setNodeName(clusterInfo.myNodeName());
                    try {
                        sendCommand.setCommandSign(SignUtils.sign(validCommand.getMessageDigestHash(), clusterInfo.privateKey()));
                    } catch (Exception e) {
                        log.error("sign error {}", e.getCause());
                        throw new RuntimeException(e);
                    }
                    sendCommand.setMessageDigest(validCommand.getMessageDigestHash());
                    sendCommand.setStatus(COMMAND_QUEUED_SEND);
                    sendCommand.setValidCommand(JSON.toJSONString(validCommand));
                    sendCommand.setCommandClass(validCommand.getClass().getSimpleName());
                    sendCommandDao.add(sendCommand);

                    clusterInfo.clusterNodeNames().forEach((toNodeName) -> {
                        SendNodePO sendNodePO = new SendNodePO();
                        sendNodePO.setMessageDigest(validCommand.getMessageDigestHash());
                        sendNodePO.setToNodeName(toNodeName);
                        sendNodePO.setStatus(SEND_NODE_WAIT_SEND);
                        sendNodeDao.add(sendNodePO);
                    });
                }
                queuedSend(sendCommand);
                //signal wait
                sendLock.lock();
                try {
                    log.info("signal the send thread");
                    sendCondition.signal();
                } finally {
                    sendLock.unlock();
                }
            }
        });
    }

    /**
     * send the validCommandWrap to server, please guaranteed thread safe
     */
    private void send() {
        while (true) {
            try {
                List<QueuedSendPO> queuedSendList = takeSendList();
                List<String> deleteMessageDigestList = new ArrayList<>();

                queuedSendList.forEach((queuedSend) -> {
                    SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(queuedSend.getMessageDigest());
                    if (null == sendCommand) {
                        escapeQueuedSend(queuedSend);
                        return;
                    }

                    if (sendCommand.getStatus().equals(COMMAND_QUEUED_GC)) {
                        log.warn("command is queued gc : {}", sendCommand);
                        return;
                    }

                    log.info("send command {}", sendCommand);
                    List<SendNodePO> sendNodeList = sendNodeDao.queryByDigestAndStatus(sendCommand.getMessageDigest(), SEND_NODE_WAIT_SEND);
                    CountDownLatch countDownLatch = new CountDownLatch(sendNodeList.size());

                    sendNodeList.forEach((sendNode) -> {
                        sendExcutorService.submit(() -> {
                            try {
                                log.info("send command to node {} ", sendNode.getToNodeName());

                                ValidCommandWrap validCommandWrap = new ValidCommandWrap();
                                validCommandWrap.setCommandClass(sendCommand.getValidCommand().getClass());
                                validCommandWrap.setFromNode(sendCommand.getNodeName());
                                validCommandWrap.setSign(sendCommand.getCommandSign());
                                validCommandWrap.setValidCommand((ValidCommand<?>) JSON.parse(sendCommand.getValidCommand()));
                                p2pConsensusClient.receiveCommand(sendNode.getToNodeName(),validCommandWrap);

                                sendNodeDao.transStatus(sendNode.getMessageDigest(), SEND_NODE_ACK);
                                sendCommand.setAckNodeNum(sendCommand.getAckNodeNum() + 1);
                            }catch (Throwable throwable){
                                log.error("{}", throwable);
                            } finally {
                                countDownLatch.countDown();
                            }
                        });
                    });

                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        log.error("{}", e);
                    }
                    sendCommandDao.updateAckNodeNum(sendCommand.getMessageDigest(), sendCommand.getAckNodeNum());

                    if (sendCommand.getAckNodeNum() >= sendCommand.getGcThreshold()) {
                        log.info("ack node num >= gc threshold, add command to gc {}", sendCommand);
                        queuedGc(sendCommand);
                    } else {
                        log.info("ack node num {} < gc threshold {}, add to delay send queue {}", sendCommand.getAckNodeNum(), sendCommand.getGcThreshold(), sendCommand);
                        queuedDelay(sendCommand);
                    }
                    deleteMessageDigestList.add(queuedSend.getMessageDigest());

                });
                if (!CollectionUtils.isEmpty(deleteMessageDigestList)) {
                    //delete with transactions
                    txRequired.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            queuedSendDao.deleteByMessageDigestList(deleteMessageDigestList);
                        }
                    });
                }
            } catch (Throwable throwable) {
                log.error("p2p send process failed", throwable);
            }
        }
    }


    /**
     * trans delay to send queue by send time, please guaranteed thread safe
     */
    public void transFromDelayToSend() {
        List<QueuedSendDelayPO> queuedSendDelayList = queuedSendDelayDao.queryListBySendTime(System.currentTimeMillis());
        if (CollectionUtils.isEmpty(queuedSendDelayList)) {
            return;
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedSendDelayList.forEach((queuedSendDelay) -> {
                    QueuedSendPO queuedSend = new QueuedSendPO();
                    queuedSend.setMessageDigest(queuedSendDelay.getMessageDigest());
                    queuedSendDao.add(queuedSend);
                    deleteMessageDigestList.add(queuedSendDelay.getMessageDigest());
                    log.info("trans message from send delay queue to send queue : {}", queuedSendDelay);
                });
                if (!CollectionUtils.isEmpty(deleteMessageDigestList)) {
                    queuedSendDelayDao.deleteByMessageDigestList(deleteMessageDigestList);
                }
            }
        });

        //signal wait
        sendLock.lock();
        try {
            log.info("signal the send thread");
            sendCondition.signal();
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * send gc, please guaranteed thread safe
     */
    public void gc() {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedSendGcPO> queuedSendGcList = queuedSendGcDao.queryGcList(System.currentTimeMillis());
                if (CollectionUtils.isEmpty(queuedSendGcList)) {
                    return;
                }
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
            }
        });
    }


    /**
     * take send list
     *
     * @return List<QueuedSendPO>
     */
    private List<QueuedSendPO> takeSendList() {
        List<QueuedSendPO> queuedSendList = queuedSendDao.querySendList();
        sendLock.lock();
        try {
            while (CollectionUtils.isEmpty(queuedSendList)) {
                sendCondition.await(5, TimeUnit.SECONDS);
                queuedSendList = queuedSendDao.querySendList();
            }
        } catch (Exception e) {
            log.error("take send list error : {}", e);
        } finally {
            sendLock.unlock();
        }
        return queuedSendList;
    }

    private void escapeQueuedSend(QueuedSendPO queuedSend) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape send command is null of messageDigest {}", queuedSend.getMessageDigest());
                queuedSendDao.deleteByMessageDigest(queuedSend.getMessageDigest());
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
        queuedSendGcPO.setGcTime(System.currentTimeMillis() + 10000L);
        queuedSendGcDao.add(queuedSendGcPO);
        sendCommandDao.transStatus(sendCommand.getMessageDigest(), COMMAND_QUEUED_GC);
    }

    private void queuedDelay(SendCommandPO sendCommand) {
        QueuedSendDelayPO queuedSendDelay = new QueuedSendDelayPO();
        queuedSendDelay.setMessageDigest(sendCommand.getMessageDigest());
        queuedSendDelay.setSendTime(System.currentTimeMillis() + 2000L);
        queuedSendDelayDao.add(queuedSendDelay);
    }

}
