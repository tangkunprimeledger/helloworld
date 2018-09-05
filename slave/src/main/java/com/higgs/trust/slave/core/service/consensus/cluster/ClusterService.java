/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.BlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ClusterHeightCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author suimi
 * @date 2018/6/11
 */
@Slf4j @Service public class ClusterService implements IClusterService {

    private static final String DEFAULT_CLUSTER_HEIGHT_ID = "cluster_height_id";

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private ValidConsensus validConsensus;

    @Autowired private IClusterViewManager viewManager;

    @Autowired private NodeState nodeState;

    /**
     * get the cluster height through consensus, the default request id will be set. if timeout, null will be return
     *
     * @param size the size of height will be consensus
     * @return
     */
    @Override public Long getClusterHeight(int size) {
        return getClusterHeight(DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis(), size);
    }

    /**
     * get the cluster height through consensus, the default request id will be set. if timeout, null will be return
     *
     * @return
     */
    @Override public Long getSafeHeight() {
        int trytimes = 20;
        ClusterView currentView = viewManager.getCurrentView();
        int treshold = 2 * currentView.getFaultNum() + 1;
        for (int i = 0; i < trytimes; i++) {
            Map<String, Long> heightMap = getAllClusterHeight();
            int size = 0;
            List<Long> heightList = new ArrayList<>();
            for (Long height : heightMap.values()) {
                if (height != null) {
                    size++;
                    heightList.add(height);
                }
            }
            if (size >= treshold) {
                log.info("get more than (2f+1) nodes' height,size:{},treshold:{}", size, treshold);
                //TODO 排序异常
                heightList = heightList.stream().sorted(Comparator.comparingLong(Long::longValue).reversed())
                    .collect(Collectors.toList());
                log.info("sorted heightList:{}", heightList);
                return heightList.get(treshold - 1);
            } else {
                log.debug("get no more than (2f+1) nodes' height");
            }
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                log.warn("self check error.", e);
            }
        }
        return null;
    }

    /**
     * get the cluster height through consensus, if timeout, null will be return
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @return
     */
    @Override public Long getClusterHeight(String requestId, int size) {
        ResponseCommand<?> responseCommand = validConsensus.submitSync(new ClusterHeightCmd(requestId, size, -1));
        return responseCommand == null ? null : (Long)responseCommand.get();
    }

    @Override public Map<String, Long> getAllClusterHeight() {
        ClusterView currentView = viewManager.getCurrentView();
        List<String> nodeNames = currentView.getNodeNames();
        Map<String, Long> heightMap = new HashMap<>();
        String requestId = DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis();
        ClusterHeightCmd cmd = new ClusterHeightCmd(requestId, 1, viewManager.getCurrentViewId());
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(nodeState.getNodeName());
        validCommandWrap.setSign(
            CryptoUtil.getProtocolCrypto().sign(cmd.getMessageDigestHash(), nodeState.getConsensusPrivateKey()));
        validCommandWrap.setValidCommand(cmd);
        nodeNames.forEach((nodeName) -> {
            Long height = null;
            try {
                ValidResponseWrap<? extends ResponseCommand> validResponseWrap =
                    p2pConsensusClient.syncSend(nodeName, validCommandWrap);
                Object response = validResponseWrap.result();
                if (response != null) {
                    if (response instanceof List) {
                        List<ResponseCommand> commands = (List)response;
                        if (!commands.isEmpty() && commands.get(0).get() != null) {
                            height = (Long)commands.get(0).get();
                        }
                    }
                }

            } catch (Throwable throwable) {
                log.error("{}", throwable);
            }
            heightMap.put(nodeName, height);
        });
        return heightMap;
    }

    /**
     * validating the block header through consensus, if timeout, null will be return
     *
     * @param header block header
     * @return
     */
    @Override public Boolean validatingHeader(BlockHeader header) {
        BlockHeaderCmd command = new BlockHeaderCmd(header, viewManager.getCurrentViewId());
        ResponseCommand<?> responseCommand = validConsensus.submitSync(command);
        return responseCommand == null ? null : (Boolean)responseCommand.get();
    }

}
