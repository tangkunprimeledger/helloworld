/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.master.command.*;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.MasterChangeListener;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Service public class ChangeMasterService implements MasterChangeListener {

    /**
     * has the master heartbeat
     */
    @Getter private AtomicBoolean masterHeartbeat = new AtomicBoolean(false);

    @Autowired private NodeProperties nodeProperties;

    @Autowired private ChangeMasterProperties properties;

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private ConsensusClient consensusClient;

    @Autowired private NodeState nodeState;

    @Autowired private INodeInfoService nodeInfoService;

    @Autowired private IClusterViewManager viewManager;

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
        masterHeartbeat.getAndSet(true);
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
        masterHeartbeat.getAndSet(false);
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        if (!nodeInfoService.hasMasterQualify()) {
            log.warn("not have master qualify");
            resetHeartbeatTimeout();
            return;
        }
        ClusterView currentView = viewManager.getCurrentView();
        Map<String, ChangeMasterVerifyResponse> responseMap = changeMasterVerify();
        if (responseMap == null || responseMap.size() < (2 * currentView.getFaultNum() + 1)) {
            resetHeartbeatTimeout();
            return;
        }
        consensusChangeMaster(nodeState.getCurrentTerm() + 1, viewManager.getCurrentViewId(), responseMap);
        resetHeartbeatTimeout();
    }

    public void artificialChangeMaster(int term, long startHeight) {
        ArtificialChangeMasterCommand command =
            new ArtificialChangeMasterCommand(term, viewManager.getCurrentViewId(), nodeState.getNodeName(),
                startHeight);
        command
            .setSign(CryptoUtil.getProtocolCrypto().sign(command.getSignValue(), nodeState.getConsensusPrivateKey()));
        CompletableFuture<?> future = consensusClient.submit(command);
        try {
            future.get(nodeProperties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("submit artificial change master to consensus failed!", e);
        }
        resetHeartbeatTimeout();
    }

    private Map<String, ChangeMasterVerifyResponse> changeMasterVerify() {
        log.info("change master verify");
        ClusterView currentView = viewManager.getCurrentView();
        List<String> nodeNames = currentView.getNodeNames();
        Map<String, ChangeMasterVerifyResponse> heightMap = new HashMap<>();
        Long maxHeight = nodeInfoService.blockHeight();
        long packageHeight = maxHeight == null ? 0 : maxHeight;
        ChangeMasterVerify verify =
            new ChangeMasterVerify(nodeState.getCurrentTerm() + 1, viewManager.getCurrentViewId(),
                nodeState.getNodeName(), packageHeight);
        ChangeMasterVerifyCmd cmd = new ChangeMasterVerifyCmd(verify);
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(nodeState.getNodeName());
        validCommandWrap
            .setSign(CryptoUtil.getProtocolCrypto().sign(cmd.getMessageDigestHash(), nodeState.getConsensusPrivateKey()));
        validCommandWrap.setValidCommand(cmd);
        nodeNames.forEach((nodeName) -> {
            try {
                ValidResponseWrap<? extends ResponseCommand> validResponseWrap =
                    p2pConsensusClient.syncSend(nodeName, validCommandWrap);
                if (validResponseWrap.isSucess()) {
                    Object response = validResponseWrap.result();
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
        ClusterView currentView = viewManager.getCurrentView();
        String pubKey = currentView.getPubKey(response.getVoter());
        if(StringUtils.isBlank(pubKey)){
            return false;
        }
        return CryptoUtil.getProtocolCrypto().verify(response.getSignValue(), response.getSign(), pubKey);
    }

    private void consensusChangeMaster(long term, long view, Map<String, ChangeMasterVerifyResponse> verifies) {
        log.info("change master, term:{}", term);
        ChangeMasterCommand command = new ChangeMasterCommand(term, view, nodeState.getNodeName(), verifies);
        command
            .setSign(CryptoUtil.getProtocolCrypto().sign(command.getSignValue(), nodeState.getConsensusPrivateKey()));
        try {
            CompletableFuture<?> future = consensusClient.submit(command);
            future.get(nodeProperties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("submit change master to consensus failed!", e);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SUBMIT_CHANGE_MASTER_COMMAND_FAILED, 1);
        }
    }

    @Override public void beforeChange(String masterName) {

    }

    @Override public void masterChanged(String masterName) {
        if (NodeState.MASTER_NA.equalsIgnoreCase(masterName)) {
            masterHeartbeat.getAndSet(false);
        }
        resetHeartbeatTimeout();
    }
}
