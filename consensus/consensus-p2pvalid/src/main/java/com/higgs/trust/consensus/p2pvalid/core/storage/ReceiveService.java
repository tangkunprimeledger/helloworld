package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.dao.*;
import com.higgs.trust.consensus.p2pvalid.dao.po.*;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component @Slf4j public class ReceiveService {

    public static final Integer COMMAND_NORMAL = 0;
    public static final Integer COMMAND_QUEUED_APPLY = 1;
    public static final Integer COMMAND_APPLIED = 2;
    public static final Integer COMMAND_QUEUED_GC = 3;

    @Autowired private ValidConsensus validConsensus;

    @Autowired private ReceiveCommandDao receiveCommandDao;

    @Autowired private ReceiveNodeDao receiveNodeDao;

    @Autowired private QueuedReceiveGcDao queuedReceiveGcDao;

    @Autowired private QueuedApplyDao queuedApplyDao;

    @Autowired private QueuedApplyDelayDao queuedApplyDelayDao;

    @Autowired private TransactionTemplate txRequired;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private NodeState nodeState;

    @Value("${p2p.revceive.increase.delay.interval:3000}") private Long delayIncreaseInterval;

    @Value("${p2p.revceive.delay.max:7200000}") private Long delayDelayMax;

    @Value("${p2p.receive.gc.interval:6000}") private Long gcInterval;

    /**
     * apply lock
     */
    private final ReentrantLock applyLock = new ReentrantLock(true);

    /**
     * apply condition
     */
    private final Condition applyCondition = applyLock.newCondition();

    /**
     * apply delay lock
     */
    private final ReentrantLock applyDelayLock = new ReentrantLock(true);

    /**
     * apply delay condition
     */
    private final Condition applyDelayCondition = applyDelayLock.newCondition();

    @PostConstruct public void initThreadPool() {
        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::apply);

        new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command apply delay thread");
            thread.setDaemon(true);
            return thread;
        }).execute(this::applyDelay);

        new ScheduledThreadPoolExecutor(1, (r) -> {
            Thread thread = new Thread(r);
            thread.setName("command receive gc thread");
            thread.setDaemon(true);
            return new Thread(r);
        }).scheduleWithFixedDelay(this::gc, gcInterval, gcInterval, TimeUnit.MILLISECONDS);

    }

    public void receive(ValidCommandWrap validCommandWrap) {
        log.info("command receive : {}", validCommandWrap);
        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new RuntimeException(String.format("the node state is not running, please try again latter"));
        }
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();

        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNode());
        if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(),
                    validCommandWrap, pubKey));
        }
        log.debug("command message digest:{}", validCommandWrap.getValidCommand().getMessageDigestHash());

        // update receive command
        ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);
        if (null == receiveCommand) {
            receiveCommand = new ReceiveCommandPO();
            //set apply threshold
            receiveCommand
                .setApplyThreshold(Math.min(clusterInfo.faultNodeNum() * 2 + 1, clusterInfo.clusterNodeNames().size()));
            receiveCommand.setCommandClass(validCommandWrap.getCommandClass().getSimpleName());
            receiveCommand.setGcThreshold(clusterInfo.clusterNodeNames().size());
            receiveCommand.setMessageDigest(messageDigest);
            receiveCommand.setNodeName(clusterInfo.nodeName());
            receiveCommand.setReceiveNodeNum(0);
            receiveCommand.setRetryApplyNum(0);
            receiveCommand.setValidCommand(JSON.toJSONString(validCommandWrap.getValidCommand()));
            receiveCommand.setStatus(COMMAND_NORMAL);
            try {
                receiveCommandDao.add(receiveCommand);
            } catch (DuplicateKeyException e) {
                //do nothing when idempotent
            }
        }

        ReceiveNodePO tempReceiveNode = receiveNodeDao
            .queryByMessageDigestAndFromNode(validCommandWrap.getValidCommand().getMessageDigestHash(),
                validCommandWrap.getFromNode());
        if (null != tempReceiveNode) {
            log.warn("duplicate command from node {} : {}", validCommandWrap.getFromNode(),
                validCommandWrap.getValidCommand());
            return;
        }

        //add receive node
        ReceiveNodePO receiveNode = new ReceiveNodePO();
        receiveNode.setCommandSign(validCommandWrap.getSign());
        receiveNode.setFromNodeName(validCommandWrap.getFromNode());
        receiveNode.setMessageDigest(validCommandWrap.getValidCommand().getMessageDigestHash());
        try {
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    receiveNodeDao.add(receiveNode);
                    receiveCommandDao.increaseReceiveNodeNum(receiveNode.getMessageDigest());
                    checkReceiveStatus(receiveNode.getMessageDigest());
                }
            });
        } catch (DuplicateKeyException e) {
            //do nothing when idempotent
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

    public void apply() {
        while (true) {
            if (!nodeState.isState(NodeStateEnum.Running)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("thread interrupted");
                }
                continue;
            }
            try {
                List<QueuedApplyPO> queuedApplyList = takeApplyList();
                queuedApplyList.forEach((queuedApply) -> {
                    Span span = TraceUtils.createSpan();
                    try {
                        log.debug("apply:{}",queuedApply);
                        ReceiveCommandPO receiveCommand =
                            receiveCommandDao.queryByMessageDigest(queuedApply.getMessageDigest());
                        if (null == receiveCommand) {
                            escapeQueuedApply(queuedApply);
                            return;
                        }

                        txRequired.execute(new TransactionCallbackWithoutResult() {
                            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                                queuedApplyDao.deleteByMessageDigest(receiveCommand.getMessageDigest());
                                applyCommand(receiveCommand);
                                log.info("command dequeue : {}", receiveCommand.getMessageDigest());
                            }
                        });
                    } finally {
                        TraceUtils.closeSpan(span);
                    }
                });
            } catch (Throwable throwable) {
                log.error(" apply error : {}", throwable);
            }
        }
    }

    public void applyDelay() {
        while (true) {
            if (!nodeState.isState(NodeStateEnum.Running)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("thread interrupted");
                }
                continue;
            }
            try {
                List<QueuedApplyDelayPO> queuedApplyDelayList = takeApplyDelayList();
                queuedApplyDelayList.forEach((queuedApplyDelay) -> {
                    Span span = TraceUtils.createSpan();
                    try {
                        log.debug("apply delay :{}",queuedApplyDelay);
                        ReceiveCommandPO receiveCommand =
                            receiveCommandDao.queryByMessageDigest(queuedApplyDelay.getMessageDigest());
                        if (null == receiveCommand) {
                            escapeQueuedApplyDelay(queuedApplyDelay);
                            return;
                        }
                        txRequired.execute(new TransactionCallbackWithoutResult() {
                            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                                queuedApplyDelayDao.deleteByMessageDigest(receiveCommand.getMessageDigest());
                                applyCommand(receiveCommand);
                                log.info("command dequeue : {}", receiveCommand.getMessageDigest());
                            }
                        });
                    } finally {
                        TraceUtils.closeSpan(span);
                    }
                });
            } catch (Throwable throwable) {
                log.error("{}", throwable);
            }
        }
    }

    /**
     * apply command
     *
     * @param receiveCommand
     */
    private void applyCommand(ReceiveCommandPO receiveCommand) {
        ValidCommit validCommit = ValidCommit.of(receiveCommand);
        log.debug("execute valid commit");
        validConsensus.getValidExecutor().execute(validCommit);

        if (receiveCommand.getStatus().equals(COMMAND_QUEUED_APPLY)) {
            log.info("command not consume by biz, retry app num {}, add command to delay queue : {}",
                receiveCommand.getRetryApplyNum(), receiveCommand);
            receiveCommandDao.increaseRetryApplyNum(receiveCommand.getMessageDigest());
            Long delayTime = (receiveCommand.getRetryApplyNum() + 1) * delayIncreaseInterval;
            delayTime = Math.min(delayTime, delayDelayMax);
            queuedDelay(receiveCommand, delayTime);

            applyDelayLock.lock();
            try {
                applyDelayCondition.signal();
            } catch (Exception e) {
                log.error("{}", e);
            } finally {
                applyDelayLock.unlock();
            }

        } else if (receiveCommand.getStatus().equals(COMMAND_APPLIED)) {

            //trans queued_apply to applied
            int count =
                receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_QUEUED_APPLY, COMMAND_APPLIED);
            if (count != 1) {
                throw new RuntimeException("trans applied status failed when apply! count: " + count);
            }

            ReceiveCommandPO receiveCommandTemp =
                receiveCommandDao.queryByMessageDigest(receiveCommand.getMessageDigest());

            if (receiveCommandTemp.getReceiveNodeNum() >= receiveCommandTemp.getGcThreshold()) {

                queuedGc(receiveCommandTemp);
                log.info(
                    "command has closed by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}",
                    receiveCommandTemp.getReceiveNodeNum(), receiveCommandTemp.getGcThreshold(), receiveCommandTemp);
            }

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
                    //delete queued gc
                    queuedReceiveGcDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete receive node
                    receiveNodeDao.deleteByMessageDigestList(deleteMessageDigestList);
                    //delete receive command
                    receiveCommandDao.deleteByMessageDigestList(deleteMessageDigestList);
                    log.info("receive gc {}", deleteMessageDigestList);
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
        applyLock.lock();
        try {
            List<QueuedApplyPO> queuedApplyList = queuedApplyDao.queryApplyList();
            while (CollectionUtils.isEmpty(queuedApplyList)) {
                applyCondition.await(5, TimeUnit.SECONDS);
                queuedApplyList = queuedApplyDao.queryApplyList();
            }
            return queuedApplyList;
        } catch (Exception e) {
            log.error("take apply list error", e);
        } finally {
            applyLock.unlock();
        }
        return new ArrayList<>();
    }

    /**
     * take apply delay list
     *
     * @return
     */
    private List<QueuedApplyDelayPO> takeApplyDelayList() {
        applyDelayLock.lock();
        try {
            List<QueuedApplyDelayPO> queuedApplyDelayList =
                queuedApplyDelayDao.queryListByApplyTime(System.currentTimeMillis());
            while (CollectionUtils.isEmpty(queuedApplyDelayList)) {
                applyDelayCondition.await(10, TimeUnit.SECONDS);
                queuedApplyDelayList = queuedApplyDelayDao.queryListByApplyTime(System.currentTimeMillis());
            }
            return queuedApplyDelayList;
        } catch (Exception e) {
            log.error("take apply delay list error", e);
        } finally {
            applyDelayLock.unlock();
        }
        return new ArrayList<>();
    }

    private void checkReceiveStatus(String messageDigest) {
        //re-query db for avoid dirty read
        ReceiveCommandPO receiveCommand = receiveCommandDao.queryByMessageDigest(messageDigest);

        //check status
        if (receiveCommand.getStatus().equals(COMMAND_QUEUED_GC)) {
            log.warn("command has add to gc : {}", receiveCommand);

        } else if (receiveCommand.getStatus().equals(COMMAND_QUEUED_APPLY)) {
            log.info("command has queued to apply : {}", receiveCommand);

        } else if (receiveCommand.getStatus().equals(COMMAND_APPLIED)) {

            if (receiveCommand.getReceiveNodeNum() >= receiveCommand.getGcThreshold()) {
                queuedGc(receiveCommand);
                log.info(
                    "command has consume by biz and receive node num :{} >=  gc threshold :{} ,add command to gc queue : {}",
                    receiveCommand.getReceiveNodeNum(), receiveCommand.getGcThreshold(), receiveCommand);
            }
        } else if (receiveCommand.getStatus().equals(COMMAND_NORMAL)) {
            if (receiveCommand.getReceiveNodeNum() >= receiveCommand.getApplyThreshold()) {
                queuedApply(receiveCommand);
                log.info(
                    "command receive node num : {} >= command apply threshold : {}, add command to apply queue : {}",
                    receiveCommand.getReceiveNodeNum(), receiveCommand.getApplyThreshold(), receiveCommand);
            } else {
                log.info("command receive ... {}", receiveCommand);
            }
        }
    }

    private void escapeQueuedApply(QueuedApplyPO queuedApply) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape apply command is null of messageDigest {}", queuedApply);
                queuedApplyDao.deleteByMessageDigest(queuedApply.getMessageDigest());
            }
        });
    }

    private void escapeQueuedApplyDelay(QueuedApplyDelayPO queuedApplyDelay) {
        //delete with transactions
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.warn("escape apply delay command is null of messageDigest {}", queuedApplyDelay);
                queuedApplyDelayDao.deleteByMessageDigest(queuedApplyDelay.getMessageDigest());
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
        //TODO 配置化，目前改成了6秒钟
        queuedReceiveGc.setGcTime(System.currentTimeMillis() + 6000L);
        queuedReceiveGc.setMessageDigest(receiveCommand.getMessageDigest());
        queuedReceiveGcDao.add(queuedReceiveGc);
        //trans status
        int count =
            receiveCommandDao.transStatus(receiveCommand.getMessageDigest(), COMMAND_APPLIED, COMMAND_QUEUED_GC);
        if (count != 1) {
            throw new RuntimeException("trans receive command status failed when add gc queue! count: " + count);
        }
    }

    private void queuedDelay(ReceiveCommandPO receiveCommand, Long delayTime) {
        //add
        QueuedApplyDelayPO queuedApplyDelay = new QueuedApplyDelayPO();
        //TODO 配置化
        queuedApplyDelay.setApplyTime(System.currentTimeMillis() + delayTime);
        queuedApplyDelay.setMessageDigest(receiveCommand.getMessageDigest());
        queuedApplyDelayDao.add(queuedApplyDelay);
    }
}
