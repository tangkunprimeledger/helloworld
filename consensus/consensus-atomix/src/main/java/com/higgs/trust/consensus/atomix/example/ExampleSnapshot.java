/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.example;

import com.higgs.trust.consensus.core.IConsensusSnapshot;
import lombok.extern.slf4j.Slf4j;

/**
 * @author suimi
 * @date 2018/8/15
 */
@Slf4j public class ExampleSnapshot implements IConsensusSnapshot {

    long currentIndex = 0;

    @Override public byte[] getSnapshot() {
        if (log.isDebugEnabled()) {
            log.debug("get snapshot index:{}", currentIndex);
        }
        return ("" + currentIndex).getBytes();
    }

    @Override public void installSnapshot(byte[] snapshot) {
        if (snapshot != null) {
            if (log.isDebugEnabled()) {
                log.debug("install snapshot:{}", snapshot);
            }
            currentIndex = Long.parseLong(new String(snapshot));
        }
    }

    public void updateIndex(long index) {
        if (this.currentIndex != index - 1) {
            log.warn("the index:{} is not allowed, current index:{}", index, this.currentIndex);
        } else {
            currentIndex = index;
        }
    }

    public long getCurrentIndex() {
        return currentIndex;
    }
}
