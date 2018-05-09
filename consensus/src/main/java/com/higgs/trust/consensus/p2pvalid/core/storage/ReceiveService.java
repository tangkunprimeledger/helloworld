package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class ReceiveService {

    private static final Integer COMMAND_NORMAL = 0;
    private static final Integer COMMAND_QUEUED_APPLY = 1;
    private static final Integer COMMAND_QUEUED_GC = 2;

    public static final Integer COMMAND_NOT_CLOSED = 0;
    public static final Integer COMMAND_CLOSED = 1;

    @Autowired
    private ValidConsensus validConsensus;

    @Autowired
    private ReceiveCommandDao receiveCommandDao;

    @Autowired
    private ReceiveNodeDao receiveNodeDao;

    @Autowired
    private QueuedReceiveGcDao queuedReceiveGcDao;

    @Autowired
    private QueuedApplyDao queuedApplyDao;

    @Autowired
    private QueuedApplyDelayDao queuedApplyDelayDao;

    @Autowired
    private TransactionTemplate txRequired;

    @Autowired
    private ClusterInfo clusterInfo;


    @Value("${p2p.revceive.delay.interval:2}")
    private Long transDelayInterval;

    @Value("${p2p.receive.gc.interval:10}")
    private Long gcInterval;

    /**
     * apply lock
     */
    private final ReentrantLock applyLock = new ReentrantLock(true);

    /**
     * apply condition
     */
    private final Condition applyCondition = applyLock.newCondition();


    @PostConstruct
    public void initThreadPool() {
        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::apply);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply trans thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::transApplyDelayToApply, transDelayInterval, transDelayInterval, TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command receive gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, gcInterval, gcInterval, TimeUnit.SECONDS);

    }

    public synchronized void receive(ValidCommandWrap validCommandWrap) {
        try {
            String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();

            //check duplicate
            ReceiveNodePO receiveNode = receiveNodeDao.queryByMessageDigestAndFromNode(validCommandWrap.getValidCommand().getMessageDigestHash(), validCommandWrap.getFromNode());

            if (null != receiveNode) {
                log.warn("duplicate command from node {} , validCommandWrap : {}", validCommandWrap.getFromNode(), validCommandWrap);
                return;
            }

            String pubKey = clusterInfo.pubKey(clusterInfo.myNodeName());
            if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
                throw new Exception(String.format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(), validCommandWrap, pubKey));
            }

            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);
                    if (null == receiveCommand) {
                        receiveCommand = new ReceiveCommandPO();
                        //set apply threshold
                        receiveCommand.setApplyThreshold(Math.min(clusterInfo.faultNodeNum() * 2 + 1, clusterInfo.clusterNodeNames().size()));
                        receiveCommand.setCommandClass(validCommandWrap.getCommandClass().getSimpleName());
                        receiveCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                        receiveCommand.setMessageDigest(messageDigest);
                        receiveCommand.setNodeName(clusterInfo.myNodeName());
                        receiveCommand.setReceiveNodeNum(1);
                        receiveCommand.setValidCommand(JSON.toJSONString(validCommandWrap.getValidCommand()));
                        receiveCommand.setStatus(COMMAND_NORMAL);
                        receiveCommand.setClosed(COMMAND_NOT_CLOSED);
                        receiveCommandDao.add(receiveCommand);
                    } else {
                        Integer receiveNodeNum = receiveCommand.getReceiveNodeNum() + 1;
                        receiveCommand.setReceiveNodeNum(receiveNodeNum);
                        receiveCommandDao.updateReceiveNodeNum(receiveCommand.getMessageDigest(), receiveNodeNum);
                    }

                    // add receive node
                    ReceiveNodePO receiveNode = new ReceiveNodePO();
                    receiveNode.setCommandSign(validCommandWrap.getSign());
                    receiveNode.setFromNodeName(validCommandWrap.getFromNode());
                    receiveNode.setMessageDigest(validCommandWrap.getValidCommand().getMessageDigestHash());
                    receiveNodeDao.add(receiveNode);

                    //check receive status
                    checkReceiveStatus(receiveCommand);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void apply() {
        while (true) {
            try {
                List<QueuedApplyPO> queuedApplyList = takeApplyList();
                queuedApplyList.forEach((queuedApply) -> {
                    ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(queuedApply.getMessageDigest());
                    if (null == receiveCommand) {
                        escapeQueuedApply(queuedApply);
                        return;
                    }
                    txRequired.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            ValidCommit validCommit = ValidCommit.of(receiveCommand);
                            validConsensus.getValidExecutor().excute(validCommit);
                            if (receiveCommand.getClosed().equals(COMMAND_NOT_CLOSED)) {
                                log.info("command not closed by biz,add command to delay queue : {}", receiveCommand);
                                queuedDeley(receiveCommand);
                            } else if (receiveCommand.getClosed().equals(COMMAND_CLOSED) && receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()) {
                                log.info("command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}", receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(), receiveCommand);
                                receiveCommandDao.updateCloseStatus(receiveCommand.getMessageDigest(), receiveCommand.getClosed());
                                queuedGc(receiveCommand);
                            }
                            queuedApplyDao.deleteByMessageDigest(queuedApply.getMessageDigest());
                        }
                    });
                });
            } catch (Throwable throwable) {
                log.error("{}", throwable);
            }
        }
    }

    public void transApplyDelayToApply() {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedApplyDelayPO> queuedApplyDelayList = queuedApplyDelayDao.queryListByApplyTime(System.currentTimeMillis());
                if (CollectionUtils.isEmpty(queuedApplyDelayList)) {
                    return;
                }
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedApplyDelayList.forEach((queuedApplyDelay) -> {
                    QueuedApplyPO queuedApply = new QueuedApplyPO();
                    queuedApply.setMessageDigest(queuedApplyDelay.getMessageDigest());
                    queuedApplyDao.add(queuedApply);
                    deleteMessageDigestList.add(queuedApply.getMessageDigest());
                    log.info("trans message from apply delay queue to apply queue : {}", queuedApplyDelay);
                });
                if (!CollectionUtils.isEmpty(deleteMessageDigestList)) {
                    queuedApplyDelayDao.deleteByMessageDigestList(deleteMessageDigestList);
                }
            }
        });
        //signal wait
        applyLock.lock();
        try {
            log.info("signal the send thread");
            applyCondition.signal();
        } finally {
            applyLock.unlock();
        }
    }

    public void gc() {
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedReceiveGcPO> queuedReceiveGcList = queuedReceiveGcDao.queryGcList(System.currentTimeMillis());
                if (CollectionUtils.isEmpty(queuedReceiveGcList)) {
                    return;
                }
                List<String> deleteMessageDigestList = new ArrayList<>();
                queuedReceiveGcList.forEach((queuedReceiveGc) -> {
                    deleteMessageDigestList.add(queuedReceiveGc.getMessageDigest());
                });

                if (!CollectionUtils.isEmpty(deleteMessageDigestList)) {
                    //delete receive command
                    receiveCommandDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete queued gc
                    queuedReceiveGcDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete receive node
                    receiveNodeDao.deleteByMessageDigestList(deleteMessageDigestList);
                }
            }
        });
    }

    /**
     * take apply list
     *
     * @return
     */
    private List<QueuedApplyPO> takeApplyList() {
        List<QueuedApplyPO> queuedApplyList = queuedApplyDao.queryApplyList();
        applyLock.lock();
        try {
            while (CollectionUtils.isEmpty(queuedApplyList)) {
                applyCondition.await(5, TimeUnit.SECONDS);
                queuedApplyList = queuedApplyDao.queryApplyList();
            }
        } catch (Exception e) {
            log.error("take apply list error : {}", e);
        } finally {
            applyLock.unlock();
        }
        return queuedApplyList;
    }

    private void checkReceiveStatus(ReceiveCommandPO receiveCommand) {
        //check status
        if (receiveCommand.getStatus().equals(COMMAND_QUEUED_GC)) {
            log.warn("command has add to gc : {}", receiveCommand);

        } else if (receiveCommand.getStatus().equals(COMMAND_QUEUED_APPLY)) {
            log.info("command has queued to apply : {}", receiveCommand);

        } else if (receiveCommand.getClosed().equals(COMMAND_CLOSED) && receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()) {
            log.info("command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}", receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(), receiveCommand);
            queuedGc(receiveCommand);
        } else if (receiveCommand.getReceiveNodeNum() >= receiveCommand.getApplyThreshold()) {
            log.info("comman receive node num : {} >= command apply threshold : {}, add command to apply queue : {}", receiveCommand.getReceiveNodeNum(), receiveCommand.getApplyThreshold(), receiveCommand);
            queuedApply(receiveCommand);
        }
    }

    private void escapeQueuedApply(QueuedApplyPO queuedApply) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape apply command is null of messageDigest {}", queuedApply.getMessageDigest());
                queuedApplyDao.deleteByMessageDigest(queuedApply.getMessageDigest());
            }
        });
    }

    private void queuedApply(ReceiveCommandPO receiveCommand) {
        QueuedApplyPO queuedApply = new QueuedApplyPO();
        queuedApply.setMessageDigest(receiveCommand.getMessageDigest());
        queuedApplyDao.add(queuedApply);
        //trans status
        receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_APPLY);
    }

    private void queuedGc(ReceiveCommandPO receiveCommand) {
        QueuedReceiveGcPO queuedReceiveGc = new QueuedReceiveGcPO();
        queuedReceiveGc.setGcTime(System.currentTimeMillis() + 10000L);
        queuedReceiveGc.setMessageDigest(receiveCommand.getMessageDigest());
        queuedReceiveGcDao.add(queuedReceiveGc);
        //trans status
        receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_GC);
    }

    private boolean queuedDeley(ReceiveCommandPO receiveCommand) {
        if (receiveCommand.getClosed().equals(COMMAND_NOT_CLOSED)) {
            log.info("command not closed by biz,add command to delay queue : {}", receiveCommand);
            //add
            QueuedApplyDelayPO queuedApplyDelay = new QueuedApplyDelayPO();
            queuedApplyDelay.setApplyTime(System.currentTimeMillis() + 2000L);
            queuedApplyDelay.setMessageDigest(receiveCommand.getMessageDigest());
            queuedApplyDelayDao.add(queuedApplyDelay);
            return true;
        }
        return false;
    }
}
