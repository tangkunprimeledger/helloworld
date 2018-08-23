/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.example;

import com.higgs.trust.consensus.core.ConsensusSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/8/15
 */
@Slf4j public class ExampleSnapshot implements ConsensusSnapshot {
    long currentIndex = 0;

    @Override public String getSnapshot() {
        if (log.isDebugEnabled()) {
            log.debug("get snapshot index:{}", currentIndex);
        }
        return "" + currentIndex;
    }

    @Override public void installSnapshot(String snapshot) {
        if (StringUtils.isNotBlank(snapshot) && StringUtils.isNumeric(snapshot)) {
            if (log.isDebugEnabled()) {
                log.debug("install snapshot:{}", snapshot);
            }
            currentIndex = Long.parseLong(snapshot);
        }
    }

    public void updateIndex(long index) {
        if (this.currentIndex != index - 1) {
            log.warn("the index:{} is not allowed, current index:{}", index, this.currentIndex);
        } else {
            currentIndex = index;
        }
    }
}
