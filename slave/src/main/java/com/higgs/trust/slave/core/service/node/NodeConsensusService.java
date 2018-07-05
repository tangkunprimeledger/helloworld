package com.higgs.trust.slave.core.service.node;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class NodeConsensusService {

    @Autowired private ConsensusStateMachine consensusStateMachine;
    @Autowired private NodeState nodeState;
    /**
     * @param user
     * @return
     * @desc after ca auth successd, start to launch consensus and failover
     */
    public void joinConsensus(String user) {

        log.info("[joinConsensus] start to join consensus layer");
        consensusStateMachine.joinConsensus();

        log.info("[joinConsensus] start to transform node status from offline to running");
        nodeState.changeState(NodeStateEnum.Offline, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);

        log.info("[joinConsensus] end join consensus layer and transform node status");

    }


    /**
     * @param user
     * @return
     * @desc after ca auth successd, start to launch consensus and failover
     */
    public void leaveConsensus(String user) {

        log.info("[leaveConsensus] start to leave consensus layer");
        consensusStateMachine.leaveConsensus();

        log.info("[leaveConsensus] start to transform node status from running to offline");
        nodeState.changeState(NodeStateEnum.Running, NodeStateEnum.Offline);

        log.info("[leaveConsensus] end leave consensus layer and transform node status");

    }



}
