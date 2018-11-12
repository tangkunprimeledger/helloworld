package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.slave.core.repository.contract.ContractStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author duhongming
 * @date 2018/05/07
 */
@Service public class DbContractStateStoreImpl implements ContractStateStore {

    @Autowired private ContractStateRepository contractStateRepository;

    @Override
    public void put(String key, Map<String,Object> state) {
        contractStateRepository.put(key,state);
    }

    @Override
    public Map<String,Object> get(String key) {
        return contractStateRepository.get(key);
    }

    @Override
    public void remove(String key) {
    }
}
