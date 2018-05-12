package com.higgs.trust.slave.core.repository.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.slave.dao.contract.ContractStateDao;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class ContractStateRepository {

    @Autowired
    ContractStateDao contractStateDao;

    public Map<String, Object> get(String address) {
        ContractStatePO po = contractStateDao.queryByAddress(address);
        if (null == po) {
            return null;
        }

        Map<String, Object> state = JSON.parseObject(po.getState());
        Map<String, Object> newState = new HashMap<>(state.size());
        state.forEach((key, value) -> newState.put(key, value));
        return newState;
    }

    public void put(String address, Map<String, Object> state) {
        ContractStatePO po = new ContractStatePO();
        po.setAddress(address);
        po.setState(JSON.toJSONString(state, StateManager.JSON_GENERATE_FEATURES));
        contractStateDao.save(po);
    }
}
