package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.integration.NodeClient;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author WangQuanzhou
 * @desc node consensus service
 * @date 2018/7/5 11:38
 */
@Service @Slf4j public class NodeConsensusService {

    @Autowired private NodeState nodeState;
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private NodeClient nodeClient;
    @Autowired private ClusterNodeRepository clusterNodeRepository;

    private static final String SUCCESS = "sucess";
    private static final String FAIL = "fail";

    /**
     * @param
     * @return
     * @desc join consensus layer
     */
    public String joinConsensus() {

        log.info("[joinConsensus] start to join consensus layer");

        ClusterNode clusterNode = clusterNodeRepository.getClusterNode(nodeState.getNodeName());
        log.info("clusterNode={}", JSON.toJSONString(clusterNode));

        if (null != clusterNode && clusterNode.isP2pStatus() == false) {
            RespData respData = nodeClient.nodeJoin(nodeState.notMeNodeNameReg(), nodeState.getNodeName());
            if (!respData.isSuccess()) {
                return FAIL;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("[joinConsensus] error occured while thread sleep", e);
                return FAIL;
            }
        }

        log.info("[joinConsensus] start to transform node status from offline to running");
        nodeState.changeState(NodeStateEnum.Offline, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
        return SUCCESS;
    }

    public RespData joinConsensusTx(String user) {

        //send and get callback result
        try {
            coreTransactionService.submitTx(constructJoinCoreTx(user));
        } catch (Throwable e) {
            log.error("send node join transaction error", e);
            return new RespData(RespCodeEnum.SYS_FAIL.getRespCode());
        }
        log.info("[joinConsensusTx] submit joinConsensusTx to slave success");
        return new RespData();
    }

    private CoreTransaction constructJoinCoreTx(String user) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(UUID.randomUUID().toString());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.NODE_JOIN.getPolicyId());
        coreTx.setActionList(buildJoinActionList(user));
        return coreTx;
    }

    private List<Action> buildJoinActionList(String user) {
        List<Action> actions = new ArrayList<>();
        NodeAction nodeAction = new NodeAction();
        nodeAction.setNodeName(user);
        nodeAction.setType(ActionTypeEnum.NODE_JOIN);
        nodeAction.setIndex(0);
        actions.add(nodeAction);
        return actions;
    }

    /**
     * @param
     * @return
     * @desc leave consensus layer
     */
    public String leaveConsensus() {

        //send and get callback result
        try {
            coreTransactionService.submitTx(constructLeaveCoreTx(nodeState.getNodeName()));
        } catch (Throwable e) {
            log.error("send node leave transaction error", e);
            return FAIL;
        }
        log.info("[leaveConsensus] submit leaveConsensusTx to slave success");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("[leaveConsensus] error occured while thread sleep", e);
            return FAIL;
        }

        log.info("[leaveConsensus] start to transform node status from running to offline");
        nodeState.changeState(NodeStateEnum.Running, NodeStateEnum.Offline);

        log.info("[leaveConsensus] end leave consensus layer and transform node status");
        return SUCCESS;

    }

    private CoreTransaction constructLeaveCoreTx(String user) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(UUID.randomUUID().toString());
        coreTx.setSender(user);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.NODE_LEAVE.getPolicyId());
        coreTx.setActionList(buildLeaveActionList(user));
        return coreTx;
    }

    private List<Action> buildLeaveActionList(String user) {
        List<Action> actions = new ArrayList<>();
        NodeAction nodeAction = new NodeAction();
        nodeAction.setNodeName(user);
        nodeAction.setType(ActionTypeEnum.NODE_LEAVE);
        nodeAction.setIndex(0);
        actions.add(nodeAction);
        return actions;
    }

}
