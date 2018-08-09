/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.master.command.*;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.term.TermManager;
import com.higgs.trust.consensus.config.listener.MasterChangeListener;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Service public class ChangeMasterService implements MasterChangeListener {

    @Autowired private NodeProperties nodeProperties;

    @Autowired private ChangeMasterProperties properties;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private ConsensusClient consensusClient;

    @Autowired private NodeState nodeState;

    @Autowired private TermManager termManager;

    @Autowired private INodeInfoService nodeInfoService;

    private ScheduledFuture heartbeatTimer;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("change master heartbeat thread");
        thread.setDaemon(true);
        return thread;
    });

    @StateChangeListener(NodeStateEnum.Running) public void startHeartbeatTimeout() {
        log.info("start change master heartbeat timeout");
        resetHeartbeatTimeout();
    }

    /**
     * renew the master heartbeat
     */
    public void renewHeartbeatTimeout() {
        log.trace("renew change master heartbeat timeout");
        termManager.setMasterHeartbeat(true);
        resetHeartbeatTimeout();
    }

    /**
     * reset the heart beat timeout, maybe no master heartbeat
     */
    private void resetHeartbeatTimeout() {
        log.trace("reset change master heartbeat timeout");
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel(true);
        }
        int masterHeartbeat = properties.getMasterHeartbeat();
        int diff = properties.getChangeMasterMaxRatio() - properties.getChangeMasterMinRatio();
        Random random = new Random();
        long delay = random.nextInt(masterHeartbeat * diff);
        delay += masterHeartbeat * properties.getChangeMasterMinRatio();
        heartbeatTimer = executor.schedule(this::changeMaster, delay, TimeUnit.MILLISECONDS);
    }

    public void changeMaster() {
        if (log.isDebugEnabled()) {
            log.debug("start change master verify");
        }
        termManager.setMasterHeartbeat(false);
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        if (!nodeInfoService.hasMasterQualify()) {
            log.warn("not have master qualify");
            resetHeartbeatTimeout();
            return;
        }
        Map<String, ChangeMasterVerifyResponse> responseMap = changeMasterVerify();
        if (responseMap == null || responseMap.size() < (2 * clusterInfo.faultNodeNum() + 1)) {
            resetHeartbeatTimeout();
            return;
        }
        consensusChangeMaster(nodeState.getCurrentTerm() + 1, responseMap);
        resetHeartbeatTimeout();
    }

    public void artificialChangeMaster(int term, long startHeight) {
        ArtificialChangeMasterCommand command =
            new ArtificialChangeMasterCommand(term, nodeState.getNodeName(), startHeight);
        command.setSign(SignUtils.sign(command.getSignValue(), nodeState.getPrivateKey()));
        CompletableFuture<Long> future = consensusClient.submit(command);
        try {
            future.get(nodeProperties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("submit artificial change master to consensus failed!", e);
        }
        resetHeartbeatTimeout();
    }

    private Map<String, ChangeMasterVerifyResponse> changeMasterVerify() {
        log.info("change master verify");
        List<String> nodeNames = clusterInfo.clusterNodeNames();
        Map<String, ChangeMasterVerifyResponse> heightMap = new HashMap<>();
        Long maxHeight = nodeInfoService.blockHeight();
        long packageHeight = maxHeight == null ? 0 : maxHeight;
        ChangeMasterVerify verify =
            new ChangeMasterVerify(nodeState.getCurrentTerm() + 1, nodeState.getNodeName(), packageHeight);
        ChangeMasterVerifyCmd cmd = new ChangeMasterVerifyCmd(verify);
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(clusterInfo.nodeName());
        validCommandWrap.setSign(SignUtils.sign(cmd.getMessageDigestHash(), clusterInfo.privateKey()));
        validCommandWrap.setValidCommand(cmd);
        nodeNames.forEach((nodeName) -> {
            try {
                ValidResponseWrap<? extends ResponseCommand> validResponseWrap =
                    p2pConsensusClient.syncSend(nodeName, validCommandWrap);
                if (validResponseWrap.isSucess()) {
                    Object response = validResponseWrap.getResult();
                    if (response instanceof ChangeMasterVerifyResponseCmd) {
                        ChangeMasterVerifyResponseCmd command = (ChangeMasterVerifyResponseCmd)response;
                        if (command.get().isChangeMaster() && verifyResponse(command)) {
                            heightMap.put(nodeName, command.get());
                        }
                    }
                }

            } catch (Throwable throwable) {
                log.error("change master verify error", throwable);
            }

        });
        return heightMap;
    }

    private boolean verifyResponse(ChangeMasterVerifyResponseCmd cmd) {
        ChangeMasterVerifyResponse response = cmd.get();
        String pubKey = clusterInfo.pubKey(response.getVoter());
        return SignUtils.verify(response.getSignValue(), response.getSign(), pubKey);
    }

    private void consensusChangeMaster(long term, Map<String, ChangeMasterVerifyResponse> verifies) {
        log.info("change master, term:{}", term);
        ChangeMasterCommand command = new ChangeMasterCommand(term, nodeState.getNodeName(), verifies);
        command.setSign(SignUtils.sign(command.getSignValue(), nodeState.getPrivateKey()));
        CompletableFuture<Map<String, ChangeMasterVerifyResponse>> future = consensusClient.submit(command);
        try {
            future.get(nodeProperties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("submit change master to consensus failed!", e);
        }
    }

    @Override public void beforeChange(String masterName) {

    }

    @Override public void masterChanged(String masterName) {
        if (NodeState.MASTER_NA.equalsIgnoreCase(masterName)) {
            termManager.setMasterHeartbeat(false);
        }
        resetHeartbeatTimeout();
    }
}
