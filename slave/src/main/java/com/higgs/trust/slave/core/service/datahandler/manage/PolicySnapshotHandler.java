package com.higgs.trust.slave.core.service.datahandler.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.service.snapshot.agent.ManageSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:24
 * @desc policy snapshot handler
 */
@Service
@Slf4j
public class PolicySnapshotHandler implements PolicyHandler {
    @Autowired
    private ManageSnapshotAgent manageSnapshotAgent;

    @Autowired
    private MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;

    @Autowired
    private RsNodeRepository rsNodeRepository;

    @Override public Policy getPolicy(String policyId) {
        // get policy from memory
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (null != initPolicyEnum) {
            List<RsNode> rsNodeList = rsNodeRepository.queryAll();
            if (CollectionUtils.isEmpty(rsNodeList)) {
                return null;
            }
            PolicyPO policyPo = new PolicyPO();
            policyPo.setPolicyId(policyId);
            policyPo.setPolicyName(initPolicyEnum.getType());
            policyPo.setDecisionType(initPolicyEnum.getDecisionType().getCode());
            List<String> rsIdList = new ArrayList<>();
            rsNodeList.forEach(rsNode->{rsIdList.add(rsNode.getRsId());});
            policyPo.setRsIds(JSON.toJSONString(rsIdList));

            return convertPolicyPOToPolicy(policyPo);
        }
        return manageSnapshotAgent.getPolicy(policyId);
    }

    @Override public void registerPolicy(RegisterPolicy registerPolicy) {
        Policy policy = manageSnapshotAgent.registerPolicy(registerPolicy);

        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.POLICY);
        if (null == merkleTree) {
            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.POLICY, new Object[]{policy});
        } else {
            merkleTreeSnapshotAgent.appendChild(merkleTree, policy);
        }
    }

    private Policy convertPolicyPOToPolicy(PolicyPO policyPO) {

        Policy policy = new Policy();
        try {
            policy.setPolicyId(policyPO.getPolicyId());
            policy.setPolicyName(policyPO.getPolicyName());
            policy.setRsIds(JSON.parseObject(policyPO.getRsIds(), new TypeReference<List<String>>() {}));
            policy.setDecisionType(DecisionTypeEnum.getBycode(policyPO.getDecisionType()));
            policy.setContractAddr(policyPO.getContractAddr());
        } catch (Throwable e) {
            log.error("json object parse exception.", e);
            return null;
        }
        return policy;
    }
}
