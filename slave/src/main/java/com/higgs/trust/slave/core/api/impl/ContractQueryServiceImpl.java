package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.ContractQueryService;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author duhongming
 * @date 2018/6/22
 */
@Slf4j
@Service
public class ContractQueryServiceImpl implements ContractQueryService {

    @Autowired
    private StandardSmartContract standardSmartContract;

    @Override
    public Object query(String address, String methodName, Object... args) {
        return standardSmartContract.executeQuery(address, methodName, args);
    }
}
