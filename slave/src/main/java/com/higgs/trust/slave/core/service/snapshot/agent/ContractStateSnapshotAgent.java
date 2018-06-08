package com.higgs.trust.slave.core.service.snapshot.agent;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * an agent for contract state snapshot
 *
 * @author duhongming
 * @date 2018-04-11
 */
@Slf4j
@Component
public class ContractStateSnapshotAgent implements CacheLoader, ContractStateStore {

    @Autowired
    MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;
    @Autowired
    private SnapshotService snapshot;
    @Autowired
    private ContractStateRepository repository;

    @Override
    public Object query(Object object) {
        ContractStateCacheKey key = (ContractStateCacheKey) object;
        Map<String, Object> state = repository.get(key.getAddress());
        if (state == null) {
            return null;
        }

        StateManager stateManager = new StateManager(state, false);
        return stateManager;
    }

    /**
     * the method to bachInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean bachInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to bachUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean bachUpdate(Map<Object, Object> updateMap) {
        return false;
    }

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    @Override
    public void put(String key, StateManager state) {
        Map<String, Object> newState = state.getState();
        Map<String, Object> oldState = state.getOldState();

        //  snapshot.put(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key), new StateManager(newState, false));

        final String tempKeyName = "__KEY__";
        newState.put(tempKeyName, key);

        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.CONTRACT);
        if (merkleTree == null) {
            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CONTRACT, new Object[]{newState});
        } else if (oldState == null) {
            merkleTreeSnapshotAgent.appendChild(merkleTree, newState);
        } else {
            oldState.put(tempKeyName, key);
            // TODO need optimize
            if (!JSON.toJSONString(oldState).equals(JSON.toJSONString(newState))) {
                merkleTreeSnapshotAgent.modifyMerkleTree(merkleTree, oldState, newState);
            }
            oldState.remove(tempKeyName);
        }
        newState.remove(tempKeyName);
    }

    @Override
    public StateManager get(String key) {
        StateManager stateManager = (StateManager) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key));
        if (stateManager == null) {
            return new StateManager();
        }

        Map<String, Object> newState = new HashMap<>(stateManager.getState().size());
        stateManager.getState().forEach((k, value) -> newState.put(k, value));
        return new StateManager(newState);
    }

    @Override
    public void remove(String key) {

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractStateCacheKey extends BaseBO {
        private String address;
    }
}
