package com.higgs.trust.slave.core.service.transaction;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
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

    public boolean verifySignatures(SignedTransaction signedTransaction, Map<String, String> rsPubKeys) {
        try {
            CoreTransaction ctx = signedTransaction.getCoreTx();

            //get policy
            InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(ctx.getPolicyId());
            List<RsPubKey> rsPubKeyList;
            if (policyEnum == null) {
                Policy policy = policyRepository.getPolicyById(ctx.getPolicyId());

                if (policy == null && CollectionUtils.isEmpty(policy.getRsIds())) {
                    log.error("acquire policy failed. policyId={}", ctx.getPolicyId());
                    return false;
                }
                rsPubKeyList = getRsPubKeyList(rsPubKeys, policy.getRsIds());
            } else {
                // default policy
                rsPubKeyList = getRsPubKeyList(rsPubKeys, null);
            }

            if (CollectionUtils.isEmpty(rsPubKeyList)) {
                log.warn("rsPubKeyList is empty. default verify pass");
                return true;
            }

            return verifyRsSign(ctx, signedTransaction.getSignatureList(), rsPubKeyList);
        } catch (Throwable e) {
            log.error("verify signatures exception. ", e);
            return false;
        }
    }

    private List<RsPubKey> getRsPubKeyList(Map<String, String> rsPubKeyMap, List<String> rsIdList) {
        if (rsPubKeyMap.isEmpty()) {
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
                if (!StringUtils.isBlank(pubKey)) {
                    rsPubKey.setRsId(rsId);
                    rsPubKey.setPubKey(rsPubKeyMap.get(rsId));
                } else {
                    throw new SlaveException(SlaveErrorEnum.SLAVE_TX_VERIFY_SIGNATURE_PUB_KEY_NOT_EXIST);
                }
            });
        }
        return rsPubKeyList;
    }

    private boolean verifyRsSign(CoreTransaction ctx, List<SignInfo> signatureList, List<RsPubKey> rsPubKeyList) {
        try {

            Map<String, String> signedMap = SignInfo.makeSignMap(signatureList);

            //check if size of signatureList less than rsIdList
            if (rsPubKeyList.size() > signatureList.size()) {
                log.error("signature size is less than need");
                return false;
            }

            //verify signature
            for (RsPubKey rsPubKey : rsPubKeyList) {
                if (null != rsPubKey && !SignUtils
                    .verify(JSON.toJSONString(ctx), signedMap.get(rsPubKey.getRsId()), rsPubKey.getPubKey())) {
                    return false;
                }
            }
        } catch (Throwable e) {
            log.error("verify signature exception. ", e);
            return false;
        }
        return true;
    }

    public boolean checkActions(CoreTransaction coreTx) {
        if (CollectionUtils.isEmpty(coreTx.getActionList())) {
            return true;
        }
        if (coreTx.getActionList().size() > 1) {
            if (InitPolicyEnum.REGISTER_POLICY.getPolicyId().equals(coreTx.getPolicyId()) || InitPolicyEnum.REGISTER_RS
                .getPolicyId().equals(coreTx.getPolicyId())) {
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
