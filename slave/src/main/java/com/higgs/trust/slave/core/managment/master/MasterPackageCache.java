package com.higgs.trust.slave.core.managment.master;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.listener.MasterChangeListener;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.repository.PendingTxRepository;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
    @Autowired private PendingTxRepository pendingTxRepository;

    private AtomicLong packHeight = new AtomicLong(0);
    private Deque<SignedTransaction> pendingTxQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedHashMap existTxMap =
        new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(Constant.MAX_EXIST_MAP_SIZE)
            .build();
    private BlockingQueue<Package> pendingPack = new LinkedBlockingDeque<>();

    @Override public void beforeChange(String masterName) {
        synchronized (this) {
            packHeight.set(0);
            pendingPack.clear();
            existTxMap.clear();
            pendingTxQueue.clear();
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

    public List<SignedTransaction> getPendingTxQueue(int count) {
        if (null == pendingTxQueue.peekFirst()) {
            return null;
        }

        int num = 0;
        List<SignedTransaction> list = new ArrayList<>();
        while (num < count) {
            SignedTransaction signedTx = pendingTxQueue.pollFirst();
            if (null != signedTx) {
                list.add(signedTx);
                num++;
            } else {
                break;
            }
        }
        return list;
    }

    public void appendDequeFirst(SignedTransaction signedTransaction) {
        pendingTxQueue.offerFirst(signedTransaction);
    }

    /**
     * @return if exist will return false
     */
    public synchronized boolean appendDequeLast(SignedTransaction signedTx) {
        String txId = signedTx.getCoreTx().getTxId();
        if (existTxMap.containsKey(txId) || pendingTxRepository.isExist(txId)) {
            return false;
        }
        pendingTxQueue.offerLast(signedTx);
        existTxMap.put(txId, txId);
        return true;
    }

    public int getPendingTxQueueSize() {
        return pendingTxQueue.size();
    }

    public void putPendingPack(Package pack) throws InterruptedException {
        synchronized (this) {
            long packageHeight = packHeight.incrementAndGet();
            pack.setHeight(packageHeight);
            pendingPack.offer(pack, 100, TimeUnit.MILLISECONDS);
        }
    }

    public int getPendingPackSize() {
        return pendingPack.size();
    }

    public Package getPackage() {
        return pendingPack.poll();
    }

}
