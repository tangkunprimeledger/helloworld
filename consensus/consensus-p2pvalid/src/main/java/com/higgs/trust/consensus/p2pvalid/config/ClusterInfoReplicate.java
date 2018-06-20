/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

/**
 * @author suimi
 * @date 2018/6/11
 */

import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@P2pvalidReplicator @Component public class ClusterInfoReplicate {

    @Autowired ClusterInfo clusterInfo;

    /**
     * handle the cluster info command
     *
     * @param commit
     */
    public ValidClusterInfoCmd handleClusterInfo(ValidSyncCommit<ClusterInfoCmd> commit) {
        ClusterInfoVo clusterInfoVo = new ClusterInfoVo();
        clusterInfoVo.setFaultNodeNum(clusterInfo.faultNodeNum());
        Map<String, String> clusters = new HashMap<>();
        clusterInfo.clusterNodeNames().forEach(nodeName -> clusters.put(nodeName, clusterInfo.pubKey(nodeName)));
        clusterInfoVo.setClusters(clusters);
        return new ValidClusterInfoCmd(commit.operation().get(), clusterInfoVo);
    }
}
