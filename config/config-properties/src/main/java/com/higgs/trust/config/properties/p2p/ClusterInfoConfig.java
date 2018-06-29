/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.properties.p2p;

import com.higgs.trust.config.p2p.AbstractClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;
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

    @Override public String pubKey(String nodeName) {
        return clusters.get(nodeName);
    }

    @Override public String privateKey() {
        return nodeState.getPrivateKey();
    }

    @Override public void init(ClusterInfoVo vo) {
        faultNodeNum = vo.getFaultNodeNum();
        clusters.clear();
        clusters.putAll(vo.getClusters());
    }

    @Override public void refresh() {

    }
}
