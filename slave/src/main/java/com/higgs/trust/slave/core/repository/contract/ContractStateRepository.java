package com.higgs.trust.slave.core.repository.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.JsonHelper;
import com.higgs.trust.slave.dao.contract.ContractStateDao;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import com.higgs.trust.slave.model.bo.contract.ContractState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class ContractStateRepository {

    @Autowired
    ContractStateDao contractStateDao;

    public boolean batchInsert(Collection<ContractStatePO> list) {
        int result = contractStateDao.batchInsert(list);
        return result == list.size();
    }

    public boolean batchUpdate(Collection<ContractStatePO> list) {
        int result = contractStateDao.batchUpdate(list);
        return result == list.size();
    }

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

    public ContractState getState(String address) {
        ContractStatePO po = contractStateDao.queryByAddress(address);
        if (null == po) {
            return null;
        }

        ContractState contractState = new ContractState();
        contractState.setAddress(po.getAddress());
        contractState.setId(po.getId());
        contractState.setState(JSON.parseObject(po.getState()));
        return contractState;
    }

    public void put(String address, Map<String, Object> state) {
        ContractStatePO po = new ContractStatePO();
        po.setAddress(address);
        po.setState(JsonHelper.serialize(state));
        contractStateDao.save(po);
    }
}
