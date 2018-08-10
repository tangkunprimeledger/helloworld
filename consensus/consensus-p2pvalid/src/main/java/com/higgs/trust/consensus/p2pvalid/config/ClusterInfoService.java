/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
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

    @Autowired private NodeProperties nodeProperties;

    @Autowired private P2pConsensusClient client;

    @Autowired private NodeState nodeState;

    @StateChangeListener(value = NodeStateEnum.Running, before = true) @Order(Ordered.HIGHEST_PRECEDENCE)
    public void refreshClusterInfo() {
        clusterInfo.refresh();
    }

    /**
     * get the cluster info through consensus, if timeout, null will be return
     */
    public void initWithCluster() {
        log.info("init clusterInfo by cluster");
        initFormAnyNode();
        ResponseCommand<?> responseCommand = null;
        int i = 0;
        do {
            responseCommand = validConsensus
                .submitSync(new ClusterInfoCmd(DEFAULT_CLUSTER_INFO_ID + "," + System.currentTimeMillis()));
            if (responseCommand == null) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("init cluster info thread interrupted", e);
                }
            }
        } while (responseCommand == null && ++i < nodeProperties.getStartupRetryTime());
        if (responseCommand == null) {
            throw new RuntimeException("init clusterInfo from cluster failed");
        }
        clusterInfo.init((ClusterInfoVo)responseCommand.get());
    }

    private void initFormAnyNode() {
        log.info("init cluster info from any node");
        ValidResponseWrap<? extends ResponseCommand> response = null;
        int i = 0;
        do {
            ClusterInfoCmd command = new ClusterInfoCmd(DEFAULT_CLUSTER_INFO_ID + "," + System.currentTimeMillis());
            ValidCommandWrap commandWrap = new ValidCommandWrap();
            commandWrap.setCommandClass(command.getClass());
            log.info("clusterInfo.nodeName={}", clusterInfo.nodeName());
            commandWrap.setFromNode(clusterInfo.nodeName());
            commandWrap.setSign(SignUtils.sign(command.getMessageDigestHash(), clusterInfo.privateKey()));
            commandWrap.setValidCommand(command);
            try {
                response = client.syncSendFeign(nodeState.notMeNodeNameReg(), commandWrap);
            } catch (Exception e) {
                log.error("get cluster info error", e);
            }
        } while ((response == null || !response.isSucess()) && ++i <= 10);
        if (response != null && response.isSucess()) {
            ValidClusterInfoCmd infoCmd = (ValidClusterInfoCmd)response.result();
            clusterInfo.init(infoCmd.get());
        } else {
            throw new RuntimeException("init clusterInfo from any node failed");
        }
    }
}
