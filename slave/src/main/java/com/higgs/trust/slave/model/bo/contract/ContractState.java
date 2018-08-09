package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author duhongming
 * @date 2018/6/12
 */
@Getter
@Setter
public class ContractState extends BaseBO implements MerkleTreeSnapshotAgent.MerkleDataNode{
    private String address;
    private Map<String, Object> state;

    @Override public String getUniqKey() {
        return address;
    }
}
