package com.higgs.trust.slave.core.service.snapshot.agent;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.ContractState;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private List<ContractStatePO> mapToContractStatePOList(Map<Object, Object> map) {
        List<ContractStatePO> list = new ArrayList<>(map.size());
        map.forEach((key, value) -> {
            ContractState contractState = (ContractState) value;
            ContractStatePO po = new ContractStatePO();
            po.setId(contractState.getId());
            po.setAddress(contractState.getAddress());
            po.setState(JSON.toJSONString(contractState.getState(), StateManager.JSON_GENERATE_FEATURES));
            list.add(po);
        });
        return list;
    }

    @Override
    public Object query(Object object) {
        ContractStateCacheKey key = (ContractStateCacheKey) object;
        ContractState state = repository.getState(key.getAddress());
        return state;
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertMap
     * @return
     */
    @Override
    public boolean batchInsert(Map<Object, Object> insertMap) {
        if (insertMap == null || insertMap.size() == 0) {
            return true;
        }

        List<ContractStatePO> list = mapToContractStatePOList(insertMap);
        return repository.batchInsert(list);
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    @Override
    public boolean batchUpdate(Map<Object, Object> updateMap) {
        if (updateMap == null || updateMap.size() == 0) {
            return true;
        }

        List<ContractStatePO> list = mapToContractStatePOList(updateMap);
        return repository.batchUpdate(list);
    }

    @Override
    public void put(String key, StateManager state) {
        Map<String, Object> newState = state.getState();
        ContractStateCacheKey cacheKey = new ContractStateCacheKey(key);
        ContractState contractState = (ContractState) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey);
        if (contractState == null) {
            contractState = new ContractState();
            contractState.setAddress(key);
            contractState.setState(newState);
            snapshot.insert(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey, contractState);
        } else {
            contractState.setState(newState);
            snapshot.update(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey, contractState);
        }

        // TODO use merkle tree

//        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.CONTRACT);
//        if (merkleTree == null) {
//            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CONTRACT, new Object[]{newState});
//        } else {
//            merkleTreeSnapshotAgent.appendChild(merkleTree, newState);
//        }
    }

    @Override
    public StateManager get(String key) {
        ContractState contractState = (ContractState) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key));
        if (contractState == null) {
            return new StateManager();
        }
        return new StateManager(contractState.getState());
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
