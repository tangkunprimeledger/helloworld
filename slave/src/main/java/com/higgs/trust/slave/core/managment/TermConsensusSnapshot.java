/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Component public class TermConsensusSnapshot implements ConsensusSnapshot {
    @Autowired private NodeState nodeState;

    @Override public String getSnapshot() {
        return JSON.toJSONString(nodeState.getTerms());
    }

    @Override public void installSnapshot(String snapshot) {
        List<TermInfo> infos = JSON.parseArray(snapshot, TermInfo.class);
        if (infos == null) {
            infos = new ArrayList<>();
        }
        nodeState.resetTerms(infos);
    }
}
