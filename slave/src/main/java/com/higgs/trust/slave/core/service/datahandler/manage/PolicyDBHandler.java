package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:24
 * @desc policy db handler
 */
@Service public class PolicyDBHandler implements PolicyHandler {

    @Autowired private PolicyRepository policyRepository;

    @Autowired private MerkleService merkleService;

    @Override public Policy getPolicy(String policyId) {
        return policyRepository.getPolicyById(policyId);
    }

    @Override public void registerPolicy(RegisterPolicy registerPolicy) {
        Policy policy = policyRepository.convertActionToPolicy(registerPolicy);
        policyRepository.save(policy);

        MerkleTree merkleTree = merkleService.queryMerkleTree(MerkleTypeEnum.POLICY);
        if (null == merkleTree) {
            merkleTree = merkleService.build(MerkleTypeEnum.POLICY, Arrays.asList(new Object[] {policy}));
        } else {
            merkleService.add(merkleTree, policy);
        }

        merkleService.flush(merkleTree);
    }
}
