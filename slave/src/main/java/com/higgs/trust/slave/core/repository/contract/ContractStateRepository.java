package com.higgs.trust.slave.core.repository.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.JsonHelper;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.mysql.contract.ContractStateDao;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import com.higgs.trust.slave.dao.rocks.contract.ContractStateRocksDao;
import com.higgs.trust.slave.model.bo.contract.ContractState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    @Autowired
    ContractStateRocksDao contractStateRocksDao;
    @Autowired InitConfig initConfig;

    public boolean batchInsert(Collection<ContractStatePO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        int result;
        if (initConfig.isUseMySQL()) {
            result = contractStateDao.batchInsert(list);
        } else {
            result = contractStateRocksDao.batchInsert(list);
        }
        return result == list.size();
    }

    public boolean batchUpdate(Collection<ContractStatePO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        int result;
        if (initConfig.isUseMySQL()) {
            result = contractStateDao.batchUpdate(list);
        } else {
            result = contractStateRocksDao.batchInsert(list);
        }
        return result == list.size();
    }

    public Map<String, Object> get(String address) {
        ContractStatePO po;
        if (initConfig.isUseMySQL()) {
            po = contractStateDao.queryByAddress(address);
        } else {
            po = contractStateRocksDao.get(address);
        }

        if (null == po) {
            return null;
        }

        Map<String, Object> state = JSON.parseObject(po.getState());
        Map<String, Object> newState = new HashMap<>(state.size());
        state.forEach((key, value) -> newState.put(key, value));
        return newState;
    }

    public ContractState getState(String address) {
        ContractStatePO po;
        if (initConfig.isUseMySQL()) {
            po = contractStateDao.queryByAddress(address);
        } else {
            po = contractStateRocksDao.get(address);
        }

        if (null == po) {
            return null;
        }

        ContractState contractState = new ContractState();
        contractState.setAddress(po.getAddress());
        contractState.setState(JSON.parseObject(po.getState()));
        contractState.setKeyDesc(po.getKeyDesc());
        return contractState;
    }

    public void put(String address,Object state) {
        ContractStatePO po = new ContractStatePO();
        po.setAddress(address);
        po.setState(JsonHelper.serialize(state));
        if (initConfig.isUseMySQL()) {
            contractStateDao.save(po);
        } else {
            contractStateRocksDao.save(po);
        }
    }
}
