/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment.master;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.managment.listener.MasterChangeListener;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.consensus.log.LogReplicateHandler;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerify;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyCmd;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponse;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponseCmd;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.Node;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author suimi
 * @date 2018/6/4
 */
@Slf4j @Service public class ChangeMasterService implements MasterChangeListener, InitializingBean {

    @Autowired private NodeProperties nodeProperties;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private LogReplicateHandler logReplicateHandler;

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private PackageRepository packageRepository;

    @Autowired private BlockRepository blockRepository;

    @Autowired private NodeState nodeState;

    private ScheduledFuture heartbeatTimer;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("change master heartbeat thread");
        thread.setDaemon(true);
        return thread;
    });

    public void startHeartbeatTimeout() {
        log.info("start change master heartbeat timeout");
        resetHeartbeatTimeout();
    }

    /**
     * renew the master heartbeat
     */
    public void renewHeartbeatTimeout() {
        log.debug("renew change master heartbeat timeout");
        nodeState.setMasterHeartbeat(true);
        resetHeartbeatTimeout();
    }

    /**
     * reset the heart beat timeout, maybe no master heartbeat
     */
    private void resetHeartbeatTimeout() {
        log.debug("reset change master heartbeat timeout");
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel(true);
        }
        int masterHeartbeat = nodeProperties.getMasterHeartbeat();
        int diff = nodeProperties.getChangeMasterMaxRatio() - nodeProperties.getChangeMasterMinRatio();
        Random random = new Random();
        long delay = random.nextInt(masterHeartbeat * diff);
        delay += masterHeartbeat * nodeProperties.getChangeMasterMinRatio();
        heartbeatTimer = executor.schedule(this::changeMaster, delay, TimeUnit.MILLISECONDS);
    }

    public void changeMaster() {
        if (log.isDebugEnabled()) {
            log.debug("start change master verify");
        }
        nodeState.setMasterHeartbeat(false);
        if (!hasQualify()) {
            resetHeartbeatTimeout();
            return;
        }
        Map<String, ChangeMasterVerifyResponse> responseMap = changeMasterVerify();
        if (responseMap == null || responseMap.size() < (2 * clusterInfo.faultNodeNum() + 1)) {
            resetHeartbeatTimeout();
            return;
        }
        logReplicateHandler.changeMaster(nodeState.getCurrentTerm() + 1, responseMap);
        resetHeartbeatTimeout();
    }

    private Map<String, ChangeMasterVerifyResponse> changeMasterVerify() {
        List<String> nodeNames = clusterInfo.clusterNodeNames();
        Map<String, ChangeMasterVerifyResponse> heightMap = new HashMap<>();
        Long maxHeight = packageRepository.getMaxHeight();
        long packageHeight = maxHeight == null ? 0 : maxHeight;
        ChangeMasterVerify verify =
            new ChangeMasterVerify(nodeState.getCurrentTerm() + 1, nodeState.getNodeName(), packageHeight);
        ChangeMasterVerifyCmd cmd = new ChangeMasterVerifyCmd(verify);
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(clusterInfo.myNodeName());
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
                log.error("{}", throwable);
            }

        });
        return heightMap;
    }

    private boolean verifyResponse(ChangeMasterVerifyResponseCmd cmd) {
        ChangeMasterVerifyResponse response = cmd.get();
        String pubKey = clusterInfo.pubKey(response.getVoter());
        return SignUtils.verify(response.getSignValue(), response.getSign(), pubKey);
    }

    /**
     * is the current node  qualified for master
     *
     * @return
     */
    private boolean hasQualify() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return false;
        }
        Long blockHeight = blockRepository.getMaxHeight();
        Long packageHeight = packageRepository.getMaxHeight();
        packageHeight = packageHeight == null ? 0 : packageHeight;
        if (blockHeight >= packageHeight) {
            return true;
        }
        List<PackageStatusEnum> packageStatusEnums = Arrays.asList(PackageStatusEnum.values());
        HashSet statusSet = new HashSet<>(packageStatusEnums);
        long count = packageRepository.count(statusSet, blockHeight);
        if (count >= packageHeight - blockHeight) {
            return true;
        }

        return false;
    }

    @Override public void masterChanged(String masterName) {
        if (NodeState.MASTER_NA.equalsIgnoreCase(masterName)) {
            nodeState.setMasterHeartbeat(false);
        }
        resetHeartbeatTimeout();
    }

    @Override public void afterPropertiesSet() {
        nodeState.registerMasterListener(this);
    }
}
