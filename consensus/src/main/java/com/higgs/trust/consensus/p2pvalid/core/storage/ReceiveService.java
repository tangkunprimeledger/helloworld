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
import org.springframework.dao.DuplicateKeyException;
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

@Component @Slf4j public class ReceiveService {

    private static final Integer COMMAND_NORMAL = 0;
    private static final Integer COMMAND_QUEUED_APPLY = 1;
    private static final Integer COMMAND_QUEUED_GC = 2;

    public static final Integer COMMAND_NOT_CLOSED = 0;
    public static final Integer COMMAND_CLOSED = 1;

    @Autowired private ValidConsensus validConsensus;

    @Autowired private ReceiveCommandDao receiveCommandDao;

    @Autowired private ReceiveNodeDao receiveNodeDao;

    @Autowired private QueuedReceiveGcDao queuedReceiveGcDao;

    @Autowired private QueuedApplyDao queuedApplyDao;

    @Autowired private QueuedApplyDelayDao queuedApplyDelayDao;

    @Autowired private TransactionTemplate txRequired;

    @Autowired private ClusterInfo clusterInfo;

    @Value("${p2p.revceive.delay.interval:2}") private Long transDelayInterval;

    @Value("${p2p.receive.gc.interval:10}") private Long gcInterval;

    /**
     * apply lock
     */
    private final ReentrantLock applyLock = new ReentrantLock(true);

    /**
     * apply condition
     */
    private final Condition applyCondition = applyLock.newCondition();

    @PostConstruct public void initThreadPool() {
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
        }).scheduleWithFixedDelay(this::transApplyDelayToApply, transDelayInterval, transDelayInterval,
            TimeUnit.SECONDS);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command receive gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, gcInterval, gcInterval, TimeUnit.SECONDS);

    }

    public void receive(ValidCommandWrap validCommandWrap) {
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();

        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNode());
        if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(),
                    validCommandWrap, pubKey));
        }


        // add receive node
        ReceiveNodePO receiveNode = new ReceiveNodePO();
        receiveNode.setCommandSign(validCommandWrap.getSign());
        receiveNode.setFromNodeName(validCommandWrap.getFromNode());
        receiveNode.setMessageDigest(validCommandWrap.getValidCommand().getMessageDigestHash());
        try {
            receiveNodeDao.add(receiveNode);
        } catch (DuplicateKeyException e) {
            //do nothing when idempotent
        }

        //TODO 考虑降低并发粒度
        synchronized (this){
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // update receive command
                    ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);
                    if (null == receiveCommand) {
                        receiveCommand = new ReceiveCommandPO();
                        //set apply threshold
                        receiveCommand.setApplyThreshold(
                                Math.min(clusterInfo.faultNodeNum() * 2 + 1, clusterInfo.clusterNodeNames().size()));
                        receiveCommand.setCommandClass(validCommandWrap.getCommandClass().getSimpleName());
                        receiveCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
                        receiveCommand.setMessageDigest(messageDigest);
                        receiveCommand.setNodeName(clusterInfo.myNodeName());
                        receiveCommand.setReceiveNodeNum(1);
                        receiveCommand.setValidCommand(JSON.toJSONString(validCommandWrap.getValidCommand()));
                        receiveCommand.setStatus(COMMAND_NORMAL);
                        receiveCommand.setClosed(COMMAND_NOT_CLOSED);
                        try {
                            receiveCommandDao.add(receiveCommand);
                        } catch (DuplicateKeyException e) {
                            //TODO DuplicateKeyException 为了保证事务读取的时候还能读到这个key，会对key加共享锁，超过两个线程同时并发时候，更新会导致死锁
                            //just increase when idempotent
                            increaseReceiveNodeNum(receiveCommand.getMessageDigest());
                        }
                    } else {
                        increaseReceiveNodeNum(receiveCommand.getMessageDigest());
                    }
                    //check receive status
                    checkReceiveStatus(receiveCommand.getMessageDigest());
                }
            });
        }
        //signal wait
        applyLock.lock();
        try {
            log.info("signal the apply thread");
            applyCondition.signal();
        } finally {
            applyLock.unlock();
        }
    }

    private void increaseReceiveNodeNum(String messageDigest) {
        int count = receiveCommandDao.increaseReceiveNodeNum(messageDigest);
        if (count != 1) {
            throw new RuntimeException("increase receive node count failed when p2p receive command! count: " + count);
        }
    }

    public void apply() {
        while (true) {
            try {
                List<QueuedApplyPO> queuedApplyList = takeApplyList();
                queuedApplyList.forEach((queuedApply) -> {
                    ReceiveCommandPO receiveCommand =
                        receiveCommandDao.queryByMessageDigest(queuedApply.getMessageDigest());
                    if (null == receiveCommand) {
                        escapeQueuedApply(queuedApply);
                        return;
                    }
                    txRequired.execute(new TransactionCallbackWithoutResult() {
                        @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                            ValidCommit validCommit = ValidCommit.of(receiveCommand);
                            validConsensus.getValidExecutor().execute(validCommit);
                            if (receiveCommand.getClosed().equals(COMMAND_NOT_CLOSED)) {
                                log.info("command not closed by biz,add command to delay queue : {}", receiveCommand);
                                queuedDelay(receiveCommand);
                            } else if (receiveCommand.getClosed().equals(COMMAND_CLOSED)
                                && receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()) {
                                log.info(
                                    "command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}",
                                    receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(),
                                    receiveCommand);
                                int count = receiveCommandDao
                                    .updateCloseStatus(receiveCommand.getMessageDigest(), COMMAND_NOT_CLOSED,
                                        receiveCommand.getClosed());
                                if (count != 1) {
                                    throw new RuntimeException(
                                        "update receive command close status failed when apply! count: " + count);
                                }
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
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<QueuedApplyDelayPO> queuedApplyDelayList =
                    queuedApplyDelayDao.queryListByApplyTime(System.currentTimeMillis());
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
            log.info("signal the apply thread");
            applyCondition.signal();
        } finally {
            applyLock.unlock();
        }
    }

    public void gc() {
        List<QueuedReceiveGcPO> queuedReceiveGcList = queuedReceiveGcDao.queryGcList(System.currentTimeMillis());
        if (CollectionUtils.isEmpty(queuedReceiveGcList)) {
            return;
        }
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
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
            log.error("take apply list error", e);
        } finally {
            applyLock.unlock();
        }
        return queuedApplyList;
    }

    private void checkReceiveStatus(String messageDigest) {
        //re-query db for avoid dirty read
        ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);

        //check status
        if (receiveCommand.getStatus().equals(COMMAND_QUEUED_GC)) {
            log.warn("command has add to gc : {}", receiveCommand);

        } else if (receiveCommand.getStatus().equals(COMMAND_QUEUED_APPLY)) {
            log.info("command has queued to apply : {}", receiveCommand);

        } else if (receiveCommand.getClosed().equals(COMMAND_CLOSED)
            && receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()) {
            log.info(
                "command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}",
                receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(), receiveCommand);
            queuedGc(receiveCommand);
        } else if (receiveCommand.getReceiveNodeNum() >= receiveCommand.getApplyThreshold()) {
            log.info("comman receive node num : {} >= command apply threshold : {}, add command to apply queue : {}",
                receiveCommand.getReceiveNodeNum(), receiveCommand.getApplyThreshold(), receiveCommand);
            queuedApply(receiveCommand);
        }
    }

    private void escapeQueuedApply(QueuedApplyPO queuedApply) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
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
        int count =
            receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_NORMAL, COMMAND_QUEUED_APPLY);
        if (count != 1) {
            throw new RuntimeException("trans receive command status failed when apply! count: " + count);
        }
    }

    private void queuedGc(ReceiveCommandPO receiveCommand) {
        QueuedReceiveGcPO queuedReceiveGc = new QueuedReceiveGcPO();
        //TODO 配置化，目前改成了10分钟
        queuedReceiveGc.setGcTime(System.currentTimeMillis() + 600000L);
        queuedReceiveGc.setMessageDigest(receiveCommand.getMessageDigest());
        queuedReceiveGcDao.add(queuedReceiveGc);
        //trans status
        int count =
            receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_APPLY, COMMAND_QUEUED_GC);
        if (count != 1) {
            throw new RuntimeException("trans receive command status failed when apply! count: " + count);
        }
    }

    private boolean queuedDelay(ReceiveCommandPO receiveCommand) {
        if (receiveCommand.getClosed().equals(COMMAND_NOT_CLOSED)) {
            log.info("command not closed by biz,add command to delay queue : {}", receiveCommand);
            //add
            QueuedApplyDelayPO queuedApplyDelay = new QueuedApplyDelayPO();
            //TODO 配置化
            queuedApplyDelay.setApplyTime(System.currentTimeMillis() + 2000L);
            queuedApplyDelay.setMessageDigest(receiveCommand.getMessageDigest());
            queuedApplyDelayDao.add(queuedApplyDelay);
            return true;
        }
        return false;
    }
}
