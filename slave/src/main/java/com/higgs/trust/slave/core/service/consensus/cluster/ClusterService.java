/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.BlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ClusterHeightCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/11
 */
@Slf4j @Service public class ClusterService implements IClusterService {

    private static final String DEFAULT_CLUSTER_HEIGHT_ID = "cluster_height_id";

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private ValidConsensus validConsensus;


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
     * get the cluster height through consensus, if timeout, null will be return
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @return
     */
    @Override public Long getClusterHeight(String requestId, int size) {
        ResponseCommand<?> responseCommand = validConsensus.submitSync(new ClusterHeightCmd(requestId, size));
        return responseCommand == null ? null : (Long)responseCommand.get();
    }

    @Override public Map<String, Long> getAllClusterHeight() {
        List<String> nodeNames = clusterInfo.clusterNodeNames();
        Map<String, Long> heightMap = new HashMap<>();
        String requestId = DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis();
        ClusterHeightCmd cmd = new ClusterHeightCmd(requestId, 1);
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(clusterInfo.nodeName());
        validCommandWrap.setSign(SignUtils.sign(cmd.getMessageDigestHash(), clusterInfo.privateKey()));
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
        BlockHeaderCmd command = new BlockHeaderCmd(header);
        ResponseCommand<?> responseCommand = validConsensus.submitSync(command);
        return responseCommand == null ? null : (Boolean)responseCommand.get();
    }

}
