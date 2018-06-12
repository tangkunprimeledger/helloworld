/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.master.command.ChangeMasterCommand;
import com.higgs.trust.config.master.command.ChangeMasterVerifyResponse;
import com.higgs.trust.config.master.command.MasterHeartbeatCommand;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.term.TermManager;
import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.core.ConsensusCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Component @Replicator public class MasterCommandReplicate {

    @Autowired NodeState nodeState;

    @Autowired TermManager termManager;

    @Autowired ChangeMasterService changeMasterService;

    @Autowired ClusterInfo clusterInfo;

    public void changeMaster(ConsensusCommit<ChangeMasterCommand> commit) {
        ChangeMasterCommand operation = commit.operation();
        if (operation.getTerm() == nodeState.getCurrentTerm() + 1) {
            Map<String, ChangeMasterVerifyResponse> verifyResponseMap = operation.get();
            List<Map.Entry<String, ChangeMasterVerifyResponse>> collect =
                verifyResponseMap.entrySet().stream().filter(e -> {
                    String pubKey = clusterInfo.pubKey(e.getKey());
                    ChangeMasterVerifyResponse value = e.getValue();
                    boolean verify = SignUtils.verify(value.getSignValue(), value.getSign(), pubKey);
                    return value.isChangeMaster() && verify && operation.getTerm() == value.getTerm() && operation
                        .getMasterName().equalsIgnoreCase(value.getProposer()) && value.getVoter()
                        .equalsIgnoreCase(e.getKey());
                }).collect(Collectors.toList());
            if (collect.size() >= (2 * clusterInfo.faultNodeNum() + 1)) {
                termManager.startNewTerm(operation.getTerm(), operation.getMasterName());
            }
        }
        commit.close();
    }

    public void masterHeartbeat(ConsensusCommit<MasterHeartbeatCommand> commit) {
        MasterHeartbeatCommand operation = commit.operation();
        if (nodeState.getCurrentTerm() == operation.get() && nodeState.getMasterName()
            .equalsIgnoreCase(operation.getNodeName())) {
            changeMasterService.renewHeartbeatTimeout();
        }
        commit.close();
    }

}
