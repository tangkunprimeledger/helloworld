package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.ManageSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:24
 * @desc policy snapshot handler
 */
@Service
public class PolicySnapshotHandler implements PolicyHandler {
    @Autowired
    private ManageSnapshotAgent manageSnapshotAgent;

    @Autowired
    private MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;

    @Override public Policy getPolicy(String policyId) {
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
}
