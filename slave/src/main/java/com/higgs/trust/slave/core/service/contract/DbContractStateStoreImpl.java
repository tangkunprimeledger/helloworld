package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/05/07
 */
@Service public class DbContractStateStoreImpl implements ContractStateStore {

    @Autowired private ContractStateRepository contractStateRepository;
    @Autowired private MerkleService merkleService;

    @Override
    public void put(String key, StateManager state) {
        Profiler.enter(String.format("put contract state:%s", key));
        contractStateRepository.put(key, state.getState());
        Map<String, Object> oldState = state.getOldState();
        MerkleTree merkleTree = merkleService.queryMerkleTree(MerkleTypeEnum.CONTRACT);
        if (merkleTree == null) {
            merkleTree = merkleService.build(MerkleTypeEnum.CONTRACT, Arrays.asList(new Object[] {state.getState()}));
        } else if (oldState == null) {
            merkleService.add(merkleTree, state.getState());
        } else  {
            merkleService.update(merkleTree, state.getOldState(), state.getState());
        }
        merkleService.flush(merkleTree);
        Profiler.release();
    }

    @Override
    public StateManager get(String key) {
        Map<String, Object> state = contractStateRepository.get(key);
        StateManager stateManager = new StateManager(state);
        return stateManager;
    }

    @Override
    public void remove(String key) {

    }
}
