package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendGcPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.QueuedSendPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.SendCommandPO;
import com.higgs.trust.consensus.p2pvalid.dao.po.SendNodePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    /**
     * send lock
     */
    private final ReentrantLock sendLock = new ReentrantLock(true);

    /**
     * send condition
     */
    private final Condition sendCondition = sendLock.newCondition();

    /**
     * @param validCommand
     * @throws Exception
     */
    public void submit(ValidCommand<?> validCommand){
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
               try{
                   SendCommandPO sendCommand = sendCommandDao.queryByMessageDigest(validCommand.messageDigest());
                   if(null != sendCommand){
                       log.warn("duplicate command {}", validCommand);
                       return;
                   }
                   sendCommand = new SendCommandPO();
                   sendCommand.setAckNodeNum(0);
                   sendCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                   sendCommand.setNodeName(clusterInfo.myNodeName());
                   sendCommand.setCommandSign(SignUtils.sign(validCommand.messageDigest(),clusterInfo.privateKey()));
                   sendCommand.setMessageDigest(validCommand.messageDigest());
                   sendCommand.setStatus(COMMAND_QUEUED_SEND);
                   sendCommand.setValidCommand(JSON.toJSONString(validCommand));
                   sendCommand.setCommandClass(validCommand.getClass().getSimpleName());
                   sendCommandDao.add(sendCommand);

                   clusterInfo.clusterNodeNames().forEach((toNodeName)->{
                       SendNodePO sendNodePO = new SendNodePO();
                       sendNodePO.setMessageDigest(validCommand.messageDigest());
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
                   try{
                       sendCondition.signal();
                   }finally {
                       sendLock.unlock();
                   }
               }catch (Throwable throwable){
                   throw new RuntimeException(throwable);
               }
            }
        });
    }

    @PostConstruct
    public void initThreadPool(){
        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command send thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::send);
    }

    /**
     * send the validCommandWrap to server
     */
    private void send() {
        while (true) {
            try{
                txRequired.execute(new TransactionCallbackWithoutResult() {
                    @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                        QueuedSendPO queuedSendPO = queuedSendDao.queryFirst();
                        sendLock.lock();
                        try{
                            while(null == queuedSendPO){
                                sendCondition.await(5, TimeUnit.SECONDS);
                                queuedSendPO = queuedSendDao.queryFirst();
                            }
                            queuedSendDao.delete(queuedSendPO.getId());
                        } catch (InterruptedException e) {
                            log.error("{}", e);
                        } finally {
                            sendLock.unlock();
                        }

                        SendCommandPO sendCommandPO = sendCommandDao.queryByMessageDigest(queuedSendPO.getMessageDigest());
                        if(null == sendCommandPO){
                            log.warn("command is null of messageDigest {}", queuedSendPO.getMessageDigest());
                            return;
                        }

                        /**
                         * check gc
                         */
                        if(sendCommandPO.getStatus().equals(COMMAND_QUEUED_GC)){
                            log.warn("command is queued gc : {}", sendCommandPO);
                            return;
                        }

                        log.info("send command {}", sendCommandPO);
                        List<SendNodePO> sendNodeList =  sendNodeDao.queryByDigestAndStatus(sendCommandPO.getMessageDigest(), SEND_NODE_WAIT_SEND);
                        CountDownLatch countDownLatch = new CountDownLatch(sendNodeList.size());

                        sendNodeList.forEach((sendNode)->{
                            log.info("send command to node {} ", sendNode.getToNodeName());
                            sendNodeDao.transStatus(sendNode.getMessageDigest(), SEND_NODE_ACK);
                            sendCommandPO.setAckNodeNum(sendCommandPO.getAckNodeNum() + 1);
                            countDownLatch.countDown();
                        });

                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            log.error("{}", e);
                        }
                        sendCommandDao.updateAckNodeNum(sendCommandPO.getMessageDigest(),sendCommandPO.getAckNodeNum());
                        if(sendCommandPO.getAckNodeNum() >= sendCommandPO.getGcThreshold()){
                            log.info("ack node num >= gc threshold, add command to gc {}", sendCommandPO);
                            QueuedSendGcPO queuedSendGcPO = new QueuedSendGcPO();
                            queuedSendGcPO.setMessageDigest(sendCommandPO.getMessageDigest());
                            queuedSendGcPO.setGcTime(System.currentTimeMillis() + 10000L);
                            queuedSendGcDao.add(queuedSendGcPO);
                            sendCommandDao.transStatus(sendCommandPO.getMessageDigest(), COMMAND_QUEUED_GC);
                        }
                    }
                });
            }catch (Throwable throwable){
                log.error("{}" ,throwable);
            }
        }
    }

}
