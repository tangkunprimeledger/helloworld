package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.manage.PolicyDao;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/02
 * @desc provide repository for business
 */
@Repository @Slf4j public class PolicyRepository {

    @Autowired private PolicyDao policyDao;

    @Autowired
    private RsNodeRepository rsNodeRepository;

    public Policy getPolicyById(String policyId) {

        if (StringUtils.isEmpty(policyId)) {
            log.error("policyId is null");
            return null;
        }

        PolicyPO policy;
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (null != initPolicyEnum) {
            List<RsNode> rsNodeList = rsNodeRepository.queryAll();

            policy = new PolicyPO();
            policy.setPolicyId(policyId);
            policy.setPolicyName(initPolicyEnum.getType());
            policy.setDecisionType(initPolicyEnum.getDecisionType().getCode());
            if (!CollectionUtils.isEmpty(rsNodeList)) {
                List<String> rsIdList = new ArrayList<>();
                rsNodeList.forEach(rsNode -> {
                    rsIdList.add(rsNode.getRsId());
                });
                policy.setRsIds(JSON.toJSONString(rsIdList));
            } else {
                policy.setRsIds(null);
            }
        } else {
            policy = policyDao.queryByPolicyId(policyId);
        }

        if (null == policy) {
            return null;
        }
        return convertPolicyPOToPolicy(policy);
    }

    public void save(Policy policy) {
        if (null == policy) {
            log.error("policy is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        policyDao.add(convertPolicyToPolicyPO(policy));
    }

    public PolicyPO convertPolicyToPolicyPO(Policy policy) {
        PolicyPO policyPO = new PolicyPO();
        policyPO.setPolicyId(policy.getPolicyId());
        policyPO.setPolicyName(policy.getPolicyName());
        policyPO.setDecisionType(policy.getDecisionType().getCode());
        policyPO.setContractAddr(policy.getContractAddr());
        policyPO.setRsIds(JSON.toJSONString(policy.getRsIds()));
        return policyPO;
    }

    public Policy convertPolicyPOToPolicy(PolicyPO policyPO) {

        Policy policy = new Policy();
        try {
            policy.setPolicyId(policyPO.getPolicyId());
            policy.setPolicyName(policyPO.getPolicyName());
            policy.setDecisionType(DecisionTypeEnum.getBycode(policyPO.getDecisionType()));
            policy.setContractAddr(policyPO.getContractAddr());
            policy.setRsIds(JSON.parseObject(policyPO.getRsIds(), new TypeReference<List<String>>() {
            }));
        } catch (Throwable e) {
            log.error("json object parse exception.", e);
            return null;
        }
        return policy;
    }

    public Policy convertActionToPolicy(RegisterPolicy action) {
        Policy policy = new Policy();
        policy.setPolicyId(action.getPolicyId());
        policy.setPolicyName(action.getPolicyName());
        policy.setDecisionType(action.getDecisionType());
        policy.setContractAddr(action.getContractAddr());
        policy.setRsIds(action.getRsIds());
        return policy;
    }

    public String getPolicyType(String policyId) {
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);

        if (null == initPolicyEnum) {
            log.error("cannot acquire policy, policyId={}", policyId);
            return null;
        }
        return initPolicyEnum.getType();
    }

    public int batchInsert(List<PolicyPO> policyPOList) {
        return policyDao.batchInsert(policyPOList);
    }
}
