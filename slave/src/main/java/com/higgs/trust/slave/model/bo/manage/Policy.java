package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tangfashuang
 * @desc policy bo
 * @date 2018-04-02
 */
@Setter @Getter public class Policy extends BaseBO implements MerkleTreeSnapshotAgent.MerkleDataNode {

    /**
     * policy id
     */
    private String policyId;

    /**
     * policy name
     */
    private String policyName;

    /**
     * rs ids of related to policy
     */
    private List<String> rsIds;
    /**
     * the decision type for vote ,1.FULL_VOTE,2.ONE_VOTE
     */
    private DecisionTypeEnum decisionType;
    /**
     * the contract address for vote rule
     */
    private String contractAddr;

    @Override public String getUniqKey() {
        return policyId;
    }
}
