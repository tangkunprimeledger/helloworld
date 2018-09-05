/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

/**
 * @author suimi
 * @date 2018/6/11
 */

import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@P2pvalidReplicator @Component public class ClusterInfoReplicate {

    @Autowired IClusterViewManager viewManager;

    /**
     * handle the cluster info command
     *
     * @param commit
     */
    public ValidClusterInfoCmd handleClusterInfo(ValidSyncCommit<ClusterInfoCmd> commit) {
        ClusterInfoCmd cmd = commit.operation();
        ClusterView view = viewManager.getView(cmd.getView());
        ClusterInfoVo clusterInfoVo = new ClusterInfoVo();
        clusterInfoVo.setFaultNodeNum(view.getFaultNum());
        Map<String, String> clusters = new HashMap<>();
        view.getNodeNames().forEach(nodeName -> clusters.put(nodeName, view.getPubKey(nodeName)));
        clusterInfoVo.setClusters(clusters);
        return new ValidClusterInfoCmd(cmd.get(), clusterInfoVo);
    }
}
