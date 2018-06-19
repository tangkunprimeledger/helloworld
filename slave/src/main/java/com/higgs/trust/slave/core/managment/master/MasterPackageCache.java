package com.higgs.trust.slave.core.managment.master;

import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.listener.MasterChangeListener;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tangfashuang
 * @date 2018/06/13 17:41
 * @desc set packHeight=null when master change
 */
@Slf4j @Service public class MasterPackageCache implements MasterChangeListener {
    private AtomicLong packHeight;
    @Autowired private BlockRepository blockRepository;
    @Autowired private PackageRepository packageRepository;
    @Autowired private NodeState nodeState;

    @Override public void masterChanged(String masterName) {
        synchronized (packHeight) {
            packHeight = null;
        }
        if (nodeState.isMaster()) {
            init();
        }
    }

    /**
     * get maxBlockHeight from db, packHeight from memory.
     * if maxBlockHeight is null, log error, return null.
     * if packHeight is null, return maxBlockHeight.(if exchange master, maxPackHeight must be initialized)
     * if package is null which height = packHeight, then return null
     * else return packHeight
     */
    private void init() {
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
            synchronized (packHeight) {
                packHeight = new AtomicLong(packageHeight);
            }
        }
    }

    public Long getPackHeight() {
        if (packHeight == null) {
            return null;
        }
        return packHeight.get();
    }

    public synchronized long increment() {
        return packHeight.getAndIncrement();
    }

}
