/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.p2p;

import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Configuration public class ClusterInfoConfig implements ClusterInfo {

    private int faultNodeNum = 0;

    @Override public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override public String nodeName() {
        return null;
    }

    @Override public List<String> clusterNodeNames() {
        return null;
    }

    @Override public String pubKey(String nodeName) {
        return null;
    }

    @Override public String privateKey() {
        return null;
    }
}
