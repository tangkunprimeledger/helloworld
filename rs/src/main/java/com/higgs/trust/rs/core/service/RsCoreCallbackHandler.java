package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.VoteRuleVO;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description
 * @date 2018-06-07
 */
@Component @Slf4j public class RsCoreCallbackHandler implements TxCallbackHandler{
    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private VoteRuleRepository voteRuleRepository;

    private TxCallbackHandler getCallbackHandler(){
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
        switch (policyEnum) {
            case REGISTER_POLICY:
                processRegisterPolicy(respData);
                return;
            case REGISTER_RS:
                return;
            case UTXO_ISSUE:
                return;
            case UTXO_DESTROY:
                return;
            case CONTRACT_ISSUE:
                return;
            case CONTRACT_DESTROY:
                return;
        }
        //call custom rs handler
        TxCallbackHandler callbackHandler = getCallbackHandler();
        callbackHandler.onEnd(respData);
    }

    @Override public void onFailOver(RespData<CoreTransaction> respData) {

    }

    /**
     * process register-policy
     *
     * @param respData
     */
    private void processRegisterPolicy(RespData<CoreTransaction> respData){
        if(!respData.isSuccess()){
            log.info("[processRegisterPolicy]register policy is fail,code:{}",respData.getRespCode());
            return;
        }
        CoreTransaction coreTransaction = respData.getData();
        VoteRule voteRule = voteRuleRepository.queryByPolicyId(coreTransaction.getPolicyId());
        if(voteRule != null){
            log.info("[processRegisterPolicy]voteRule is already exist,txId:{}",coreTransaction.getTxId());
            return;
        }
        JSONObject jsonObject = coreTransaction.getBizModel();
        // parse and save policy rule
        VoteRuleVO voteRuleVO = JSON.parseObject(jsonObject.toJSONString(), VoteRuleVO.class);
        voteRule = new VoteRule();
        voteRule.setPolicyId(coreTransaction.getPolicyId());
        voteRule.setVotePattern(VotePatternEnum.fromCode(voteRuleVO.getVotePattern()));
        voteRule.setCallbackType(CallbackTypeEnum.fromCode(voteRuleVO.getCallbackType()));
        voteRuleRepository.add(voteRule);
    }
}
