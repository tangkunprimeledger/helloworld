package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
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
import sun.security.provider.SHA;

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

    }

    /**
     * @param validCommand
     * @throws Exception
     */
    public void submit(ValidCommand<?> validCommand) {
        SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(validCommand.getMessageDigestHash());
        if (null != sendCommand) {
            log.warn("duplicate command {}", validCommand);
            return;
        }

        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                SendCommandPO sendCommand = new SendCommandPO();
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

                //add send queue
                QueuedSendPO queuedSendPO = new QueuedSendPO();
                queuedSendPO.setMessageDigest(validCommand.messageDigest());
                queuedSendDao.add(queuedSendPO);

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
                //TODO chengwenyan 事务开大了，整个队列开了一个事务，应该一个send请求一个队列
                txRequired.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        List<QueuedSendPO> queuedSendPOList = queuedSendDao.querySendList();
                        sendLock.lock();
                        try {
                            while (CollectionUtils.isEmpty(queuedSendPOList)) {
                                sendCondition.await(5, TimeUnit.SECONDS);
                                queuedSendPOList = queuedSendDao.querySendList();
                            }
                        } catch (InterruptedException e) {
                            log.error("{}", e);
                        } finally {
                            sendLock.unlock();
                        }

                        List<String> deleteMessageDigestList = new ArrayList<>();
                        queuedSendPOList.forEach((queuedSend)->{
                            SendCommandPO sendCommandPO = sendCommandDao.queryByMessageDigest(queuedSend.getMessageDigest());
                            if (null == sendCommandPO) {
                                log.warn("command is null of messageDigest {}", queuedSend.getMessageDigest());
                                sendCommandDao.deleteByMessageDigest(queuedSend.getMessageDigest());
                                sendNodeDao.deleteByMessageDigest(queuedSend.getMessageDigest());
                                return;
                            }
                            /**
                             * check gc
                             */
                            if (sendCommandPO.getStatus().equals(COMMAND_QUEUED_GC)) {
                                log.warn("command is queued gc : {}", sendCommandPO);
                                return;
                            }
                            log.info("send command {}", sendCommandPO);

                            List<SendNodePO> sendNodeList = sendNodeDao.queryByDigestAndStatus(sendCommandPO.getMessageDigest(), SEND_NODE_WAIT_SEND);
                            CountDownLatch countDownLatch = new CountDownLatch(sendNodeList.size());

                            sendNodeList.forEach((sendNode) -> {
                                try{
                                    log.info("send command to node {} ", sendNode.getToNodeName());
                                    sendNodeDao.transStatus(sendNode.getMessageDigest(), SEND_NODE_ACK);
                                    sendCommandPO.setAckNodeNum(sendCommandPO.getAckNodeNum() + 1);
                                }finally {
                                    countDownLatch.countDown();
                                }
                            });

                            try {
                                countDownLatch.await();
                            } catch (InterruptedException e) {
                                log.error("{}", e);
                            }
                            sendCommandDao.updateAckNodeNum(sendCommandPO.getMessageDigest(), sendCommandPO.getAckNodeNum());
                            if (sendCommandPO.getAckNodeNum() >= sendCommandPO.getGcThreshold()) {
                                log.info("ack node num >= gc threshold, add command to gc {}", sendCommandPO);
                                QueuedSendGcPO queuedSendGcPO = new QueuedSendGcPO();
                                queuedSendGcPO.setMessageDigest(sendCommandPO.getMessageDigest());
                                queuedSendGcPO.setGcTime(System.currentTimeMillis() + 10000L);
                                queuedSendGcDao.add(queuedSendGcPO);
                                sendCommandDao.transStatus(sendCommandPO.getMessageDigest(), COMMAND_QUEUED_GC);
                            } else {
                                log.info("ack node num {} < gc threshold {}, add to delay send queue {}", sendCommandPO.getAckNodeNum(), sendCommandPO.getGcThreshold(), sendCommandPO);
                                QueuedSendDelayPO queuedSendDelay = new QueuedSendDelayPO();
                                queuedSendDelay.setMessageDigest(sendCommandPO.getMessageDigest());
                                queuedSendDelay.setSendTime(System.currentTimeMillis() + 2000L);
                                queuedSendDelayDao.add(queuedSendDelay);
                            }
                            deleteMessageDigestList.add(queuedSend.getMessageDigest());
                        });
                        if(!deleteMessageDigestList.isEmpty()){
                            queuedSendDao.deleteByMessageDigestList(deleteMessageDigestList);
                        }
                    }
                });
            } catch (Throwable throwable) {
                log.error("p2p send process failed", throwable);
            }
        }
    }


    /**
     * trans delay to send queue by send time, please guaranteed thread safe
     */
    public void transFromDelayToSend() {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedSendDelayPO> queuedSendDelayList = queuedSendDelayDao.queryListBySendTime(System.currentTimeMillis());
                if(null == queuedSendDelayList || queuedSendDelayList.isEmpty()){
                    return;
                }
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedSendDelayList.forEach((queuedSendDelay) -> {
                    QueuedSendPO queuedSend = new QueuedSendPO();
                    queuedSend.setMessageDigest(queuedSendDelay.getMessageDigest());
                    queuedSendDao.add(queuedSend);
                    deleteMessageDigestList.add(queuedSendDelay.getMessageDigest());
                    log.info("trans message from send delay queue to send queue : {}", queuedSendDelay);
                });
                //signal wait
                sendLock.lock();
                try {
                    log.info("signal the send thread");
                    sendCondition.signal();
                } finally {
                    sendLock.unlock();
                }
                if(!deleteMessageDigestList.isEmpty()){
                    queuedSendDelayDao.deleteByMessageDigestList(deleteMessageDigestList);
                }
            }
        });
    }

    /**
     * send gc, please guaranteed thread safe
     */
    public void gc() {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedSendGcPO> queuedSendGcList = queuedSendGcDao.queryGcList(System.currentTimeMillis());
                if(null == queuedSendGcList || queuedSendGcList.isEmpty()){
                    return;
                }
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedSendGcList.forEach((queuedSendGc)->{
                    deleteMessageDigestList.add(queuedSendGc.getMessageDigest());
                });

                if(!deleteMessageDigestList.isEmpty()){
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

}
