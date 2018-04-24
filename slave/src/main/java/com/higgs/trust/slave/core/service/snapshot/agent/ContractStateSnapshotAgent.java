package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import com.higgs.trust.slave.model.bo.snapshot.CacheKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * an agent for contract state snapshot
 * @author duhongming
 * @date 2018-04-11
 */
@Slf4j @Component public class ContractStateSnapshotAgent implements CacheLoader, ContractStateStore {

    @Autowired private SnapshotService snapshot;
    @Autowired private ContractStateRepository repository;
    @Autowired MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;

    @Override public Object query(Object object) {
        ContractStateCacheKey key = (ContractStateCacheKey) object;
        Map<String, Object> state = repository.get(key.getAddress());
        StateManager stateManager = new StateManager(state);
        return stateManager;
    }

    @Override public void put(String key, StateManager state) {
        snapshot.put(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key), state);
        Map<String, Object> oldState = state.getOldState();
        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.CONTRACT);
        if (merkleTree == null) {
            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CONTRACT, new Object[] {state.getState()});
        } else if (oldState == null){
            merkleTreeSnapshotAgent.appendChild(merkleTree, state.getState());
        } else {
            merkleTreeSnapshotAgent.modifyMerkleTree(merkleTree, oldState, state.getState());
        }
    }

    @Override public StateManager get(String key) {
        return (StateManager) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key));
    }

    @Override public void remove(String key) {

    }

    @Getter @Setter @AllArgsConstructor
    public class ContractStateCacheKey extends CacheKey {
        private String address;
    }
}
