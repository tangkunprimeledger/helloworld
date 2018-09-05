/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.master.command.ArtificialChangeMasterCommand;
import com.higgs.trust.config.master.command.ChangeMasterCommand;
import com.higgs.trust.config.master.command.ChangeMasterVerifyResponse;
import com.higgs.trust.config.master.command.MasterHeartbeatCommand;
import com.higgs.trust.config.snapshot.TermManager;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.core.ConsensusCommit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Slf4j @Component @Replicator public class MasterCommandReplicate {

    @Autowired NodeState nodeState;

    @Autowired TermManager termManager;

    @Autowired IClusterViewManager viewManager;

    @Autowired ChangeMasterService changeMasterService;

    public void changeMaster(ConsensusCommit<ChangeMasterCommand> commit) {
        log.debug("received change master commit");
        ChangeMasterCommand operation = commit.operation();
        if (operation.getTerm() == nodeState.getCurrentTerm() + 1) {
            ClusterView currentView = viewManager.getCurrentView();
            Map<String, ChangeMasterVerifyResponse> verifyResponseMap = operation.get();
            List<Map.Entry<String, ChangeMasterVerifyResponse>> collect =
                verifyResponseMap.entrySet().stream().filter(e -> {
                    String pubKey = currentView.getPubKey(e.getKey());
                    ChangeMasterVerifyResponse value = e.getValue();
                    boolean verify = false;
                    try {
                        verify = CryptoUtil.getProtocolCrypto().verify(value.getSignValue(), value.getSign(), pubKey);
                    } catch (Throwable throwable) {
                        log.warn("verify sign of {} failed", e.getKey());
                    }
                    return value.isChangeMaster() && verify && operation.getTerm() == value.getTerm()
                        && operation.getView() == viewManager.getCurrentViewId() && operation.getMasterName()
                        .equalsIgnoreCase(value.getProposer()) && value.getVoter().equalsIgnoreCase(e.getKey());
                }).collect(Collectors.toList());
            if (collect.size() >= (2 * currentView.getFaultNum() + 1)) {
                if (log.isDebugEnabled()) {
                    log.debug("new term is {}, new master is {}", operation.getTerm(), operation.getMasterName());
                }
                termManager.startNewTerm(operation.getTerm(), operation.getMasterName());
            }
        }
        commit.close();
    }

    public void artificialChangeMaster(ConsensusCommit<ArtificialChangeMasterCommand> commit) {
        log.debug("received change master commit");
        ArtificialChangeMasterCommand operation = commit.operation();
        long currentViewId = viewManager.getCurrentViewId();
        if (operation.getView() != currentViewId) {
            log.error("command view:{} not equals current view:{}", operation.getView(), currentViewId);
            commit.close();
            return;
        }
        termManager.getTerms().clear();
        termManager.startNewTerm(operation.getTerm(), operation.getMasterName(), operation.getStartHeight());
        commit.close();
    }

    public void masterHeartbeat(ConsensusCommit<MasterHeartbeatCommand> commit) {
        MasterHeartbeatCommand operation = commit.operation();
        if (nodeState.getCurrentTerm() == operation.get() && viewManager.getCurrentViewId() == operation.getView()
            && nodeState.getMasterName().equalsIgnoreCase(operation.getNodeName())) {
            changeMasterService.renewHeartbeatTimeout();
        }
        commit.close();
    }

}
