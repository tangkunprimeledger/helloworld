package com.higgs.trust.slave.core.service.transaction;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/03/38
 * @desc check tx include verify signature
 */
@Slf4j @Component public class TxCheckHandler {

    @Autowired private PolicyRepository policyRepository;

    @Autowired private RsPubKeyRepository rsPubKeyRepository;

    /**
     * register repository need all rs sign
     */
    private static String ALL = "ALL";

    public boolean verifySignatures(SignedTransaction signedTransaction) {
        try {
            if (null == signedTransaction || null == signedTransaction.getCoreTx()) {
                log.error("signed transaction is not valid");
                return false;
            }

            //TODO 如果master节点启动的时候，需注册RS，此时数据库中没有存任何公私钥，需放开校验

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
                rsPubKeyList = getRsPubKeyList(policy.getRsIds());
            } else {
                // default policy
                rsPubKeyList = rsPubKeyRepository.queryAll();
            }

            if (CollectionUtils.isEmpty(rsPubKeyList)) {
                log.error("verify signatures failed. rsPubKeyList is empty.");
                return false;
            }

            return verifyRsSign(ctx, signedTransaction.getSignatureList(), rsPubKeyList);
        } catch (Throwable e) {
            log.error("verify signatures exception. ", e);
            return false;
        }
    }

    private List<RsPubKey> getRsPubKeyList(List<String> rsIdList) {
        try {

            if (rsIdList.size() < 1) {
                return null;
            }

            List<RsPubKey> rsPubKeyList = new ArrayList<>();
            for (String rsId : rsIdList) {
                RsPubKey rsPubKey = rsPubKeyRepository.queryByRsId(rsId);

                //if rsId cannot acquire public key, policy exception.
                if (null == rsPubKey) {
                    log.error("acquire rsPubKey exception. rsPubKey is null. rsId={}", rsId);
                    return null;
                }
                rsPubKeyList.add(rsPubKey);
            }
            return rsPubKeyList;
        } catch (Throwable e) {
            log.error("get rs pub key exception. ", e);
            return null;
        }
    }

    private boolean verifyRsSign(CoreTransaction ctx, List<String> signatureList, List<RsPubKey> rsPubKeyList) {
        try {
            //check if size of signatureList less than rsIdList
            if (rsPubKeyList.size() > signatureList.size()) {
                log.error("signature size is less than need");
                return false;
            }

            List<String> signList = new ArrayList<>();
            CollectionUtils.addAll(signList, signatureList);
            //cycle verify signature
            for (RsPubKey rsPubKey : rsPubKeyList) {
                if (null != rsPubKey) {
                    //flag represents verifying signature success or fail
                    boolean flag = false;
                    for (String sign : signList) {
                        flag = SignUtils.verify(JSON.toJSONString(ctx), sign, rsPubKey.getPubKey());
                        //if true, break this cycle, remove signature from signatureList,
                        //next cycle will not verify this signature
                        if (flag) {
                            signList.remove(sign);
                            break;
                        }
                    }
                    //if cycle complete, but flag is false, represent verify signature failed.
                    if (!flag) {
                        return false;
                    }
                }
            }
        } catch (Throwable e) {
            log.error("verify signature exception. ", e);
            return false;
        }
        return true;
    }

    public boolean checkActions(CoreTransaction coreTx) {
        if(CollectionUtils.isEmpty(coreTx.getActionList())) {
            return true;
        }

        if (InitPolicyEnum.REGISTER.getPolicyId().equals(coreTx.getPolicyId())
                && coreTx.getActionList().size() > 1) {
            return false;
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
