/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.term;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.config.node.TermInfo;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Component public class TermConsensusSnapshot implements ConsensusSnapshot {
    @Autowired private TermManager termManager;

    @Override public String getSnapshot() {
        String snapshot = JSON.toJSONString(termManager.getTerms());
        log.info("get snapshot:{}", snapshot);
        return snapshot;
    }

    @Override public void installSnapshot(String snapshot) {
        log.info("install snapshot:{}", snapshot);
        List<TermInfo> infos = JSON.parseArray(snapshot, TermInfo.class);
        if (infos == null) {
            infos = new ArrayList<>();
        }
        termManager.resetTerms(infos);
    }
}
