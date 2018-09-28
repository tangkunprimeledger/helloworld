package com.higgs.trust.slave.core.managment.master;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.listener.MasterChangeListener;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tangfashuang
 * @date 2018/06/13 17:41
 * @desc set packHeight=null when master change
 */
@Slf4j @Service public class MasterPackageCache implements MasterChangeListener {

    @Autowired private BlockRepository blockRepository;
    @Autowired private PackageRepository packageRepository;
    @Autowired private NodeState nodeState;

    private AtomicLong packHeight = new AtomicLong(0);
    private Deque<TermedTransaction> pendingTxQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedHashMap existTxMap =
        new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(Constant.MAX_EXIST_MAP_SIZE)
            .build();
    private BlockingQueue<PackageCommand> pendingPack = new LinkedBlockingDeque<>();

    @Override public void beforeChange(String masterName) {
        synchronized (this) {
            packHeight.set(0);
            pendingPack.clear();
            initPackHeight();
        }
    }

    @Override public void masterChanged(String masterName) {

    }

    /**
     * get maxBlockHeight from db, packHeight from memory.
     * if maxBlockHeight is null, log error, return null.
     * if packHeight is null, return maxBlockHeight.(if exchange master, maxPackHeight must be initialized)
     * if package is null which height = packHeight, then return null
     * else return packHeight
     */
    private void initPackHeight() {
        Long maxBlockHeight = blockRepository.getMaxHeight();
        //genius block must be exist
        if (null == maxBlockHeight) {
            log.error("please initialize genius block");
            return;
        }

        //when exchange master, packHeight must be null
        if (null == packHeight || 0 == packHeight.get()) {
            Long maxPackHeight = packageRepository.getMaxHeight();
            long packageHeight = maxBlockHeight;
            if (null != maxPackHeight) {
                packageHeight = maxBlockHeight > maxPackHeight ? maxBlockHeight : maxPackHeight;
            }
            synchronized (this) {
                log.info("set master package height:{}", packageHeight);
                packHeight.set(packageHeight);
            }
        }
    }

    public Long getPackHeight() {
        if (packHeight == null) {
            return null;
        }

        return packHeight.get();
    }

    public Object[] getPendingTxQueue(int count) {
        if (null == pendingTxQueue.peekFirst()) {
            return null;
        }

        //TODO 压测分析日志
        log.info("pendingTxQueue.size={}", pendingTxQueue.size());
        Object[] objs = new Object[2];
        int num = 0;
        List<SignedTransaction> list = new ArrayList<>();
        Set<String> txIdSet = new HashSet<>();
        long currentTerm = nodeState.getCurrentTerm();
        while (num++ < count) {
            TermedTransaction termedTransaction = pendingTxQueue.pollFirst();
            if(termedTransaction == null){
                break;
            }
            if(termedTransaction.getCurrentTerm() != currentTerm){
                existTxMap.remove(termedTransaction.getTx().getCoreTx().getTxId());
                continue;
            }
            SignedTransaction signedTx = termedTransaction.getTx();
            if (!txIdSet.contains(signedTx.getCoreTx().getTxId())) {
                list.add(signedTx);
                txIdSet.add(signedTx.getCoreTx().getTxId());
                TxTypeEnum txTypeEnum = TxTypeEnum.getBycode(signedTx.getCoreTx().getTxType());
                //for consensus
                if(txTypeEnum!=null && txTypeEnum == TxTypeEnum.NODE){
                    objs[1] = signedTx;
                    break;
                }
            }
        }
        objs[0] = list;
        return objs;
    }

    public void appendDequeFirst(SignedTransaction signedTransaction) {
        pendingTxQueue.offerFirst(new TermedTransaction(signedTransaction,nodeState.getCurrentTerm()));
    }

    /**
     * @return if exist will return false
     */
    public boolean appendDequeLast(SignedTransaction signedTx) {
        String txId = signedTx.getCoreTx().getTxId();
        if (existTxMap.containsKey(txId)) {
            return false;
        }
        pendingTxQueue.offerLast(new TermedTransaction(signedTx,nodeState.getCurrentTerm()));
        existTxMap.put(txId, txId);
        return true;
    }

    public int getPendingTxQueueSize() {
        return pendingTxQueue.size();
    }

    public void putPendingPack(PackageCommand command) throws InterruptedException {
        synchronized (this) {
            try {
                long packageHeight = packHeight.incrementAndGet();
                command.get().setHeight(packageHeight);
                pendingPack.offer(command, 100, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                //set packHeight
                packHeight.getAndDecrement();
                throw e;
            }
        }
    }

    public int getPendingPackSize() {
        return pendingPack.size();
    }

    public PackageCommand getPackage() {
        return pendingPack.poll();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class TermedTransaction{
        /**
         * the transaction
         */
        SignedTransaction tx;
        /**
         * the term of cluster
         */
        long currentTerm;
    }
}
