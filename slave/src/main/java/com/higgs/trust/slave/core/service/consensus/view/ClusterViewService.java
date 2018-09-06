/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.view;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/19
 */
@Slf4j @Service public class ClusterViewService {

    private static final String DEFAULT_CLUSTER_INFO_ID = "cluster_info_id";

    @Autowired private ValidConsensus validConsensus;

    @Autowired private NodeProperties nodeProperties;

    @Autowired private P2pConsensusClient client;

    @Autowired private NodeState nodeState;

    @Autowired private IClusterViewManager viewManager;

    @Autowired private CaRepository caRepository;

    @Autowired private BlockRepository blockRepository;

    @Autowired private PackageRepository packageRepository;

    @StateChangeListener(value = NodeStateEnum.SelfChecking, before = true) @Order(Ordered.HIGHEST_PRECEDENCE)
    public void loadClusterView() {
        List<Ca> list = caRepository.getAllCa();
        Map<String, String> consensusNodeMap = new HashMap<>();
        list.stream().filter(ca -> ca.getUsage().equals(UsageEnum.CONSENSUS.getCode()))
            .forEach(ca -> consensusNodeMap.put(ca.getUser(), ca.getPubKey()));
        Long maxBlockHeight = blockRepository.getMaxHeight();
        Long maxPackageHeight = packageRepository.getMaxHeight();
        long height =
            Math.max(maxBlockHeight == null ? 0 : maxBlockHeight, maxPackageHeight == null ? 0 : maxPackageHeight);
        viewManager.resetViews(Collections.singletonList(new ClusterView(0, height, consensusNodeMap)));
    }

    /**
     * get the cluster info through consensus, if timeout, null will be return
     */
    public void initWithCluster() {
        log.info("init clusterInfo by cluster");
        initFromAnyNode();
        ResponseCommand<?> responseCommand = null;
        int i = 0;
        do {
            responseCommand = validConsensus.submitSync(
                new ClusterViewCmd(DEFAULT_CLUSTER_INFO_ID + "," + System.currentTimeMillis(),
                    IClusterViewManager.START_CLUSTER_VIEW_ID));
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
        viewManager.setStartView((ClusterView)responseCommand.get());
    }

    private void initFromAnyNode() {
        log.info("init cluster info from any node");
        ValidResponseWrap<? extends ResponseCommand> response = null;
        int i = 0;
        do {
            ClusterViewCmd command = new ClusterViewCmd(DEFAULT_CLUSTER_INFO_ID + "," + System.currentTimeMillis(),
                IClusterViewManager.CURRENT_VIEW_ID);
            ValidCommandWrap commandWrap = new ValidCommandWrap();
            commandWrap.setCommandClass(command.getClass());
            commandWrap.setFromNode(nodeState.getNodeName());
            commandWrap.setSign(CryptoUtil.getProtocolCrypto()
                .sign(command.getMessageDigestHash(), nodeState.getConsensusPrivateKey()));
            commandWrap.setValidCommand(command);
            try {
                response = client.syncSendFeign(nodeState.notMeNodeNameReg(), commandWrap);
            } catch (Exception e) {
                log.error("get cluster info error", e);
            }
        } while ((response == null || !response.isSucess()) && ++i <= 10);
        if (response != null && response.isSucess()) {
            ValidClusterViewCmd viewCmd = (ValidClusterViewCmd)response.result();
            viewManager.setStartView(viewCmd.get());
            log.info("init cluster info from any node successful");
        } else {
            throw new RuntimeException("init clusterInfo from any node failed");
        }
    }
}
