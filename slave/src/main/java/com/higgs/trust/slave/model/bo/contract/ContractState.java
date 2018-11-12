package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/6/12
 */
@Getter
@Setter
public class ContractState extends BaseBO implements MerkleTreeSnapshotAgent.MerkleDataNode{
    private String address;
    private Object state;
    private String keyDesc;

    @Override public String getUniqKey() {
        return address;
    }
}
