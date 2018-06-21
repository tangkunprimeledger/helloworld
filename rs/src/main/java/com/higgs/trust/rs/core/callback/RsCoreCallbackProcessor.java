package com.higgs.trust.rs.core.callback;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description
 * @date 2018-06-07
 */
@Component @Slf4j public class RsCoreCallbackProcessor implements TxCallbackHandler {
    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private VoteRuleRepository voteRuleRepository;
    @Autowired private ConfigRepository configRepository;
    @Autowired private NodeState nodeState;

    private TxCallbackHandler getCallbackHandler() {
        TxCallbackHandler txCallbackHandler = txCallbackRegistor.getCoreTxCallback();
        if (txCallbackHandler == null) {
            log.error("[getCallbackHandler]call back handler is not register");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
        }
        return txCallbackHandler;
    }

    @Override public void onVote(VotingRequest votingRequest) {
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onVote(votingRequest);
    }

    @Override public void onPersisted(RespData<CoreTransaction> respData) {
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onPersisted(respData);
    }

    @Override public void onEnd(RespData<CoreTransaction> respData) {
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
                case CONTRACT_DESTROY:
                    return;
                case CA_UPDATE:
                    processCaUpdate(respData);
                    return;
                case CA_CANCEL:
                    processCaCancel(respData);
                    return;
                default:
                    break;
            }
        }
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onEnd(respData);
    }

    @Override public void onFailover(RespData<CoreTransaction> respData) {
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
                default:
                    break;
            }
        }
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onFailover(respData);
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
        RegisterPolicy registerPolicy = (RegisterPolicy)coreTransaction.getActionList().get(0);
        //check
        VoteRule voteRule = voteRuleRepository.queryByPolicyId(registerPolicy.getPolicyId());
        if (voteRule != null) {
            log.info("[processRegisterPolicy]voteRule already exist policyId:{},txId:{}", registerPolicy.getPolicyId(),
                coreTransaction.getTxId());
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
            return;
        }

        CoreTransaction coreTransaction = respData.getData();
        String user = coreTransaction.getSender();
        if (!StringUtils.equals(user, nodeState.getNodeName())) {
            log.info("[processCaUpdate] current node ={}, is not ca updated user={}, end update pubKey/priKey",
                nodeState.getNodeName(), user);
        }

        log.info("[processCaUpdate] start to update pubKey/priKey, nodeName={}", user);
        // update table config, set tmpKey to key
        Config config = configRepository.getConfig(user);
        config.setPubKey(config.getTmpPubKey());
        config.setPriKey(config.getTmpPriKey());
        configRepository.updateConfig(config);
        log.info("[processCaUpdate] end update pubKey/priKey, nodeName={}", user);
    }

    private void processCaCancel(RespData<CoreTransaction> respData) {
        if (!respData.isSuccess()) {
            log.info("[processCaCancel]ca cancel is fail,code:{}", respData.getRespCode());
            return;
        }

        log.info("[processCaCancel] start to invalid pubKey/priKey, nodeName={}", nodeState.getNodeName());
        //set pubKey and priKey to invalid
        Config config = new Config();
        CoreTransaction coreTransaction = respData.getData();
        String user = coreTransaction.getSender();
        config.setNodeName(user);
        config.setValid(false);
        configRepository.updateConfig(config);
        log.info("[processCaCancel] end invalid pubKey/priKey, nodeName={}", nodeState.getNodeName());
    }
}
