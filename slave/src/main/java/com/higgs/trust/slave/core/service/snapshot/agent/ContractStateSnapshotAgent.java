package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.common.utils.MD5;
import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.JsonHelper;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.ContractState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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

    private List<ContractStatePO> listToContractStatePOList(List<Pair<Object, Object>> updateList) {
        List<ContractStatePO> list = new ArrayList<>(updateList.size());
        updateList.forEach(pair -> {
            ContractState contractState = (ContractState) pair.getRight();
            ContractStatePO po = new ContractStatePO();
            po.setAddress(contractState.getAddress());
            po.setState(JsonHelper.serialize(contractState.getState()));
            po.setKeyDesc(contractState.getKeyDesc());
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
     * @param insertList
     * @return
     */
    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        if (CollectionUtils.isEmpty(insertList)){
            return true;
        }

        List<ContractStatePO> list = listToContractStatePOList(insertList);
        return repository.batchInsert(list);
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateList
     * @return
     */
    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        if (CollectionUtils.isEmpty(updateList)){
            return true;
        }

        List<ContractStatePO> list = listToContractStatePOList(updateList);
        return repository.batchUpdate(list);
    }

    @Override
    public void put(String key, Object newState) {
        ContractStateCacheKey cacheKey = new ContractStateCacheKey(key,key);
        ContractState contractState = (ContractState) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey);
        if (contractState == null) {
            contractState = new ContractState();
            contractState.setAddress(cacheKey.getAddress());
            contractState.setState(newState);
            contractState.setKeyDesc(cacheKey.getKeyDesc());
            snapshot.insert(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey, contractState);
        } else {
            contractState.setState(newState);
            snapshot.update(SnapshotBizKeyEnum.CONTRACT_SATE, cacheKey, contractState);
        }

        merkleTreeSnapshotAgent.addNode(MerkleTypeEnum.CONTRACT, contractState);
    }

    @Override
    public Object get(String key) {
        ContractState contractState = (ContractState) snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE, new ContractStateCacheKey(key,key));
        if (contractState == null) {
            return new HashMap<String,Object>();
        }
        return contractState.getState();
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
        private String keyDesc;
        /**
         * get address for key
         * @return
         */
        public String getAddress(){
            if(StringUtils.isEmpty(address)){
                return address;
            }
            return makeNewKey(address);
        }
        /**
         * make new key by md5
         * @param key
         * @return
         */
        private String makeNewKey(String key){
            return MD5.encode(key);
        }
    }
}
