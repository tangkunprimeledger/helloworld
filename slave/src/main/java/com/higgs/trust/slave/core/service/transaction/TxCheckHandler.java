package com.higgs.trust.slave.core.service.transaction;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author tangfashuang
 * @date 2018/03/38
 * @desc check tx include verify signature
 */
@Slf4j @Component public class TxCheckHandler {

    @Autowired private PolicyRepository policyRepository;
    @Autowired private CaRepository caRepository;

    /**
     * verify signatures
     *
     * @param signedTransaction
     * @param rsPubKeys
     * @return
     */
    public boolean verifySignatures(SignedTransaction signedTransaction, Map<String, String> rsPubKeys) {
        try {
            CoreTransaction ctx = signedTransaction.getCoreTx();
            //get policy
            InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(ctx.getPolicyId());
            List<RsPubKey> rsPubKeyList = null;
            DecisionTypeEnum decisionType;
            if (policyEnum == null) {
                Policy policy = policyRepository.getPolicyById(ctx.getPolicyId());
                if (policy == null || CollectionUtils.isEmpty(policy.getRsIds())) {
                    log.error("acquire policy failed. policyId={}", ctx.getPolicyId());
                    return false;
                }
                decisionType = policy.getDecisionType();
                rsPubKeyList = getRsPubKeyList(rsPubKeys, policy.getRsIds());
            } else {
                // default policy
                decisionType = policyEnum.getDecisionType();
                rsPubKeyList = getRsPubKeyList(rsPubKeys, null);
            }
            //for node operation
            if (TxTypeEnum.isTargetType(ctx.getTxType(), TxTypeEnum.NODE)) {
                List<Action> actionList = ctx.getActionList();
                NodeAction action = (NodeAction)actionList.get(0);
                //make sign info by action
                SignInfo signInfo = new SignInfo();
                signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);
                signInfo.setSign(action.getSelfSign());
                signInfo.setOwner(action.getNodeName());
                //verify self sign
                boolean r = signVerify(action.getSignValue(), signInfo, action.getPubKey());
                if (!r) {
                    log.error("verify node action sign has error, signValue:{},signInfo:{},pubKey:{}", action.getSignValue(),
                        signInfo, action.getPubKey());
                    return false;
                }
                //reset pubkey by all 'CONSENSUS' from cluster
                if(rsPubKeyList!=null){
                    rsPubKeyList.clear();
                }
                rsPubKeyList = caRepository.getAllPubkeyByUsage(UsageEnum.CONSENSUS);
            }
            if (CollectionUtils.isEmpty(rsPubKeyList)) {
                log.warn("rsPubKeyList is empty. default verify pass");
                return true;
            }
            Profiler.enter("[doVerifySign]");
            return verifyRsSign(ctx, signedTransaction.getSignatureList(), rsPubKeyList, decisionType);
        } catch (Throwable e) {
            log.error("verify signatures exception. ", e);
            return false;
        } finally {
            Profiler.release();
        }
    }

    private List<RsPubKey> getRsPubKeyList(Map<String, String> rsPubKeyMap, List<String> rsIdList) {
        if (null == rsPubKeyMap || rsPubKeyMap.isEmpty()) {
            return null;
        }

        List<RsPubKey> rsPubKeyList = new ArrayList<>();
        if (CollectionUtils.isEmpty(rsIdList)) {
            rsPubKeyMap.forEach((k, v) -> {
                RsPubKey rsPubKey = new RsPubKey();
                rsPubKey.setRsId(k);
                rsPubKey.setPubKey(v);
                rsPubKeyList.add(rsPubKey);
            });
        } else {
            rsIdList.forEach(rsId -> {
                RsPubKey rsPubKey = new RsPubKey();
                String pubKey = rsPubKeyMap.get(rsId);
                if (!StringUtils.isEmpty(pubKey)) {
                    rsPubKey.setRsId(rsId);
                    rsPubKey.setPubKey(rsPubKeyMap.get(rsId));
                    rsPubKeyList.add(rsPubKey);
                } else {
                    throw new SlaveException(SlaveErrorEnum.SLAVE_TX_VERIFY_SIGNATURE_PUB_KEY_NOT_EXIST);
                }
            });
        }
        return rsPubKeyList;
    }

    /**
     * verify all sign
     *
     * @param ctx
     * @param signatureList
     * @param rsPubKeyList
     * @param decisionType
     * @return
     */
    private boolean verifyRsSign(CoreTransaction ctx, List<SignInfo> signatureList, List<RsPubKey> rsPubKeyList,
        DecisionTypeEnum decisionType) {
        boolean flag = false;
        try {
            String signValue = JSON.toJSONString(ctx);
            Map<String, SignInfo> signedMap = SignInfo.makeSignMap(signatureList);
            if (DecisionTypeEnum.FULL_VOTE == decisionType) {
                //check if size of signatureList less than rsIdList
                if (rsPubKeyList.size() > signatureList.size()) {
                    log.error("signature size is less than need");
                    return false;
                }
                //verify signature
                for (RsPubKey rsPubKey : rsPubKeyList) {
                    if (null != rsPubKey && !signVerify(signValue, signedMap.get(rsPubKey.getRsId()),
                        rsPubKey.getPubKey())) {
                        log.error("verifyRsSign has error signValue:{},signInfo:{},pubKey:{}", signValue,
                            signedMap.get(rsPubKey.getRsId()), rsPubKey.getPubKey());
                        return false;
                    }
                }
                flag = true;
            } else if (DecisionTypeEnum.ONE_VOTE == decisionType) {
                for (RsPubKey rsPubKey : rsPubKeyList) {
                    if (null != rsPubKey && signVerify(signValue, signedMap.get(rsPubKey.getRsId()),
                        rsPubKey.getPubKey())) {
                        return true;
                    }
                }
                flag = false;
            }
        } catch (Throwable e) {
            log.error("verify signature exception. ", e);
            return false;
        }
        return flag;
    }

    /**
     * sign verify
     *
     * @param signInfo
     * @return
     */
    private boolean signVerify(String signValue, SignInfo signInfo, String pubKey) {
        if (signInfo.getSignType() == SignInfo.SignTypeEnum.CONSENSUS) {
            return CryptoUtil.getProtocolCrypto().verify(signValue, signInfo.getSign(), pubKey);
        } else {
            return CryptoUtil.getBizCrypto(null).verify(signValue, signInfo.getSign(), pubKey);
        }
    }

    public boolean checkActions(CoreTransaction coreTx) {
        if (CollectionUtils.isEmpty(coreTx.getActionList())) {
            return true;
        }
        if (coreTx.getActionList().size() > 1) {
            if (InitPolicyEnum.REGISTER_POLICY.getPolicyId().equals(coreTx.getPolicyId()) || InitPolicyEnum.REGISTER_RS
                .getPolicyId().equals(coreTx.getPolicyId()) || InitPolicyEnum.CANCEL_RS.getPolicyId()
                .equals(coreTx.getPolicyId())) {
                return false;
            }
        }
        return checkActionsIndex(coreTx.getActionList());
    }

    private boolean checkActionsIndex(final List<Action> actionList) {
        List<Action> sortActionList = new ArrayList<>();
        CollectionUtils.addAll(sortActionList, actionList);

        Collections.sort(sortActionList, new Comparator<Action>() {
            @Override public int compare(Action a1, Action a2) {
                return a1.getIndex().compareTo(a2.getIndex());
            }
        });

        for (int i = 0; i < sortActionList.size(); i++) {
            Action action = sortActionList.get(i);
            if (action.getIndex() != i) {
                return false;
            }
        }
        return true;
    }
}
