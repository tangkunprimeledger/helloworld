/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.p2p;

import com.higgs.trust.consensus.config.NodeState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Getter @Setter @ConfigurationProperties(prefix = "higgs.trust.p2p") @Configuration public class ClusterInfoConfig
    extends AbstractClusterInfo {

    @Autowired private NodeState nodeState;

    private int faultNodeNum = 0;

    private Map<String, String> clusters;

    @Override public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override public String nodeName() {
        return nodeState.getNodeName();
    }

    @Override public List<String> clusterNodeNames() {
        return new ArrayList<>(clusters.keySet());
    }

    @Override public String pubKeyForConsensus(String nodeName) {
        return clusters.get(nodeName);
    }

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    @Override public String pubKeyForBiz(String nodeName) {
        return null;
    }

    @Override public String priKeyForConsensus() {
        return nodeState.getPrivateKey();
    }

    /**
     * get the self private key
     *
     * @return
     */
    @Override public String priKeyForBiz() {
        return null;
    }

    @Override public void init(ClusterInfoVo vo) {
        faultNodeNum = vo.getFaultNodeNum();
        clusters.clear();
        clusters.putAll(vo.getClusters());
    }

    @Override public void refresh() {

    }

    @Override public void refreshConsensus() {

    }
}
