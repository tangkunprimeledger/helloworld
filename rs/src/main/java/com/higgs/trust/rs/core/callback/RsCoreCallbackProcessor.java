package com.higgs.trust.rs.core.callback;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-07
 */
@Component
@Slf4j
public class RsCoreCallbackProcessor implements TxCallbackHandler {
    @Autowired
    private TxCallbackRegistor txCallbackRegistor;
    @Autowired
    private VoteRuleRepository voteRuleRepository;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private NodeState nodeState;
    @Autowired
    private ConsensusStateMachine consensusStateMachine;

    private TxCallbackHandler getCallbackHandler() {
        TxCallbackHandler txCallbackHandler = txCallbackRegistor.getCoreTxCallback();
        if (txCallbackHandler == null) {
            log.error("[getCallbackHandler]call back handler is not register");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
        }
        return txCallbackHandler;
    }

    @Override
    public void onVote(VotingRequest votingRequest) {
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onVote(votingRequest);
    }

    @Override
    public void onPersisted(RespData<CoreTransaction> respData, BlockHeader blockHeader) {
        CoreTransaction coreTransaction = respData.getData();
        String policyId = coreTransaction.getPolicyId();
        InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (policyEnum != null) {
            switch (policyEnum) {
                case NA:
                    return;
                case REGISTER_POLICY:
                    processRegisterPolicy(respData);
                    return;
                case REGISTER_RS:
                    return;
                case CONTRACT_ISSUE:
                    return;
                case CONTRACT_INVOKE:
                    return;
                case CONTRACT_DESTROY:
                    return;
                case CA_UPDATE:
                    processCaUpdate(respData);
                    return;
                case CA_CANCEL:
                    processCaCancel(respData);
                    return;
                case CA_AUTH:
                    processCaAuth(respData);
                    return;
                case CANCEL_RS:
                    return;
                case NODE_JOIN:
                    processNodeJoin(respData);
                    return;
                case NODE_LEAVE:
                    processNodeLeave(respData);
                    return;
                default:
                    break;
            }
        }
        try {
            Profiler.enter("[rs.custom.callback.onPersisted]");
            TxCallbackHandler callbackHandler = getCallbackHandler();
            callbackHandler.onPersisted(respData, blockHeader);
        } finally {
            Profiler.release();
        }
    }

    @Override
    public void onEnd(RespData<CoreTransaction> respData, BlockHeader blockHeader) {
        CoreTransaction coreTransaction = respData.getData();
        String policyId = coreTransaction.getPolicyId();
        InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (policyEnum != null) {
            switch (policyEnum) {
                case NA:
                    return;
                case REGISTER_POLICY:
                    return;
                case REGISTER_RS:
                    return;
                case CONTRACT_ISSUE:
                    return;
                case CONTRACT_INVOKE:
                    return;
                case CONTRACT_DESTROY:
                    return;
                case CA_UPDATE:
                    return;
                case CA_CANCEL:
                    return;
                case CA_AUTH:
                    return;
                case CANCEL_RS:
                    return;
                case NODE_JOIN:
                    return;
                case NODE_LEAVE:
                    processNodeLeave(respData);
                    return;
                default:
                    break;
            }
        }
        try {
            Profiler.enter("[rs.custom.callback.onEnd]");
            TxCallbackHandler callbackHandler = getCallbackHandler();
            callbackHandler.onEnd(respData, blockHeader);
        } finally {
            Profiler.release();
        }
    }

    @Override
    public void onFailover(RespData<CoreTransaction> respData, BlockHeader blockHeader) {
        CoreTransaction coreTransaction = respData.getData();
        String policyId = coreTransaction.getPolicyId();
        InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (policyEnum != null) {
            switch (policyEnum) {
                case NA:
                case REGISTER_RS:
                case CONTRACT_ISSUE:
                case CONTRACT_DESTROY:
                case CONTRACT_INVOKE:
                case CA_UPDATE:
                case CA_CANCEL:
                case CA_AUTH:
                case CANCEL_RS:
                case NODE_JOIN:
                case NODE_LEAVE:
                    return;
                case REGISTER_POLICY:
                    processRegisterPolicy(respData);
                    return;
                default:
                    break;
            }
        }
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onFailover(respData, blockHeader);
    }

    /**
     * process register-policy
     *
     * @param respData
     */
    private void processRegisterPolicy(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processRegisterPolicy]register policy is fail,code:{}", respData.getRespCode());
            return;
        }
        CoreTransaction coreTransaction = respData.getData();
        //get register policy action
        RegisterPolicy registerPolicy = (RegisterPolicy) coreTransaction.getActionList().get(0);
        //check
        VoteRule voteRule = voteRuleRepository.queryByPolicyId(registerPolicy.getPolicyId());
        if (voteRule != null) {
            log.info("[processRegisterPolicy]voteRule already exist policyId:{},txId:{}", registerPolicy.getPolicyId(), coreTransaction.getTxId());
            return;
        }
        JSONObject jsonObject = coreTransaction.getBizModel();

        voteRule = new VoteRule();
        voteRule.setPolicyId(registerPolicy.getPolicyId());
        voteRule.setVotePattern(VotePatternEnum.fromCode(jsonObject.getString("votePattern")));
        voteRule.setCallbackType(CallbackTypeEnum.fromCode(jsonObject.getString("callbackType")));
        voteRuleRepository.add(voteRule);
    }


    private void processCaUpdate(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processCaUpdate]ca update is fail,code:{}", respData.getRespCode());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_UPDATE_ERROR, 1);
            return;
        }

        CoreTransaction coreTransaction = respData.getData();
        String user = coreTransaction.getSender();
        if (!StringUtils.equals(user, nodeState.getNodeName())) {
            log.info("[processCaUpdate] current node ={}, is not ca updated user={}, end update pubKey/priKey", nodeState.getNodeName(), user);
            return;
        }

        log.info("[processCaUpdate] start to update pubKey/priKey, nodeName={}", user);
        // update table config, set tmpKey to key
        Config config = configRepository.getBizConfig(user);
        if (null == config) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_UPDATE_ERROR, "ca update error");
        }
        config.setPubKey(config.getTmpPubKey());
        config.setPriKey(config.getTmpPriKey());
        config.setValid(true);
        configRepository.updateConfig(config);
        log.info("[processCaUpdate] end update pubKey/priKey, nodeName={}", user);
    }

    private void processCaCancel(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processCaCancel]ca cancel is fail,code:{}", respData.getRespCode());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_CANCEL_ERROR, 1);
            return;
        }

        CoreTransaction coreTransaction = respData.getData();
        String user = coreTransaction.getSender();
        if (!StringUtils.equals(user, nodeState.getNodeName())) {
            log.info("[processCaCancel] current node ={}, is not ca cancel user={}, end cancel pubKey/priKey", nodeState.getNodeName(), user);
            return;
        }
        
        log.info("[processCaCancel] start to invalid pubKeyForConsensus/priKey, nodeName={}", nodeState.getNodeName());
        //set pubKeyForConsensus and priKey to invalid
        Config config = configRepository.getBizConfig(user);
        if (null == config) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_UPDATE_ERROR, "ca update error");
        }
        config.setValid(false);
        configRepository.updateConfig(config);

        log.info("[processCaCancel] end invalid pubKey/priKey, nodeName={}", nodeState.getNodeName());
    }

    private void processCaAuth(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processCaAuth]ca auth is fail,code:{}", respData.getRespCode());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_AUTH_ERROR, 1);
            return;
        }
    }

    private void processNodeJoin(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processNodeJoin]node join is fail,code:{}", respData.getRespCode());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_NODE_JOIN_ERROR, 1);
            return;
        }
    }

    private void processNodeLeave(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processNodeLeave]node leave is fail,code:{}", respData.getRespCode());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_NODE_LEAVE_ERROR, 1);
            return;
        }
        List<Action> actionList = respData.getData().getActionList();
        if (CollectionUtils.isNotEmpty(actionList)) {
            Action action = actionList.get(0);
            if (action instanceof NodeAction) {
                NodeAction nodeAction = (NodeAction) action;
                if (StringUtils.equals(nodeState.getNodeName(), nodeAction.getNodeName()) && action.getType() == ActionTypeEnum.NODE_LEAVE && nodeState.isState(NodeStateEnum.Running)) {
                    log.info("leave consensus layer, user={}", nodeAction.getNodeName());
                    consensusStateMachine.leaveConsensus();
                    nodeState.changeState(NodeStateEnum.Running, NodeStateEnum.Offline);
                }
            }
        }
    }
}
