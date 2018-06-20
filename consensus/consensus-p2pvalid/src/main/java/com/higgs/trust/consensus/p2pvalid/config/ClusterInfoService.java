/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.listener.StateChangeListener;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author suimi
 * @date 2018/6/19
 */
@Slf4j @Service public class ClusterInfoService {

    private static final String DEFAULT_CLUSTER_INFO_ID = "cluster_info_id";

    @Autowired ClusterInfo clusterInfo;

    @Autowired private ValidConsensus validConsensus;

    @StateChangeListener(NodeStateEnum.Running) @Order(Ordered.HIGHEST_PRECEDENCE) public void refreshClusterInfo() {
        clusterInfo.refresh();
    }

    /**
     * get the cluster info through consensus, if timeout, null will be return
     */
    public void initWithCluster() {
        log.info("init the clusterInfo by cluster");
        ResponseCommand<?> responseCommand = null;
        do {
            responseCommand = validConsensus
                .submitSync(new ClusterInfoCmd(DEFAULT_CLUSTER_INFO_ID + "," + System.currentTimeMillis()));
        } while (responseCommand != null);
        clusterInfo.init((ClusterInfoVo)responseCommand.get());
    }
}
