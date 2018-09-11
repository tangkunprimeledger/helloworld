package com.higgs.trust.rs.core.service;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.rs.core.api.CaService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.core.integration.NodeClient;
import com.higgs.trust.rs.core.vo.NodeOptVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import com.higgs.trust.slave.model.enums.UsageEnum;
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

    @Autowired private ConsensusStateMachine consensusStateMachine;
    @Autowired private SignService signService;
    @Autowired private ConfigRepository configRepository;
    @Autowired private CaService caService;

    private static final String SUCCESS = "sucess";
    private static final String FAIL = "fail";

    public String joinRequest() {
        log.info("[joinRequest] send ca auth request");
        caService.authKeyPair(nodeState.getNodeName());
        log.info("[joinRequest] end send ca auth request");

        log.info("[joinRequest] send join consensus request");
        String nodeName = nodeState.getNodeName();
        NodeOptVO vo = new NodeOptVO();
        vo.setNodeName(nodeName);
        //add pubKey
        Config config = configRepository.getConfig(nodeName, UsageEnum.CONSENSUS);
        String pubKey = config.getPubKey();
        String signValue = nodeName + "-" + pubKey;
        String sign = signService.sign(signValue, SignInfo.SignTypeEnum.CONSENSUS);
        vo.setPubKey(pubKey);
        vo.setSign(sign);
        vo.setSignValue(signValue);
        RespData respData = nodeClient.nodeJoin(nodeState.notMeNodeNameReg(), vo);
        if (!respData.isSuccess()) {
            log.error("resp = {}", respData);
            return FAIL;
        }
        log.info("[joinRequest] end send join consensus request");
        return SUCCESS;
    }

    /**
     * @param
     * @return
     * @desc join consensus layer
     */
    public String joinConsensus() {
        log.info("[joinConsensus] start to join consensus layer");

        try {
            consensusStateMachine.joinConsensus();
        } catch (Throwable e) {
            log.error("[joinConsensus] join consensus error", e);
            nodeState.changeState(nodeState.getState(), NodeStateEnum.Offline);
            return FAIL;
        }
        log.info("[joinConsensus] end join consensus layer");
        return SUCCESS;
    }

    /**
     * process join request
     *
     * @param vo
     * @return
     */
    public RespData joinConsensusTx(NodeOptVO vo) {
        //send and get callback result
        try {
            coreTransactionService.submitTx(constructJoinCoreTx(vo));
        } catch (Throwable e) {
            log.error("send node join transaction error", e);
            return new RespData(RespCodeEnum.SYS_FAIL.getRespCode());
        }
        log.info("[joinConsensusTx] submit joinConsensusTx to slave success");
        return new RespData();
    }

    /**
     * make core transaction
     *
     * @param vo
     * @return
     */
    private CoreTransaction constructJoinCoreTx(NodeOptVO vo) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(UUID.randomUUID().toString());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.NODE_JOIN.getPolicyId());
        coreTx.setActionList(buildJoinActionList(vo));
        //set transaction type
        coreTx.setTxType(TxTypeEnum.NODE.getCode());
        return coreTx;
    }

    /**
     * make action
     *
     * @param vo
     * @return
     */
    private List<Action> buildJoinActionList(NodeOptVO vo) {
        List<Action> actions = new ArrayList<>();

        NodeAction nodeAction = new NodeAction();
        nodeAction.setType(ActionTypeEnum.NODE_JOIN);
        nodeAction.setIndex(0);
        nodeAction.setNodeName(vo.getNodeName());
        nodeAction.setSelfSign(vo.getSign());
        nodeAction.setSignValue(vo.getSignValue());
        nodeAction.setPubKey(vo.getPubKey());
        actions.add(nodeAction);

        return actions;
    }

    /**
     * @param
     * @return
     * @desc process leave consensus layer
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

    /**
     * make core transaction
     *
     * @param nodeName
     * @return
     */
    private CoreTransaction constructLeaveCoreTx(String nodeName) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(UUID.randomUUID().toString());
        coreTx.setSender(nodeName);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.NODE_LEAVE.getPolicyId());
        coreTx.setActionList(buildLeaveActionList(nodeName));
        //set transaction type
        coreTx.setTxType(TxTypeEnum.NODE.getCode());
        return coreTx;
    }

    /**
     * make action
     *
     * @param nodeName
     * @return
     */
    private List<Action> buildLeaveActionList(String nodeName) {
        List<Action> actions = new ArrayList<>();
        NodeAction nodeAction = new NodeAction();
        nodeAction.setType(ActionTypeEnum.NODE_LEAVE);
        nodeAction.setIndex(0);
        nodeAction.setNodeName(nodeName);
        //add pubKey
        Config config = configRepository.getConfig(nodeName, UsageEnum.CONSENSUS);
        String pubKey = config.getPubKey();
        String signValue = nodeName + "-" + pubKey;
        String sign = signService.sign(signValue, SignInfo.SignTypeEnum.CONSENSUS);
        nodeAction.setPubKey(pubKey);
        nodeAction.setSelfSign(sign);
        nodeAction.setSignValue(signValue);
        actions.add(nodeAction);
        return actions;
    }

}
