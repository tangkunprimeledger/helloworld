package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.ContractQueryService;
import com.higgs.trust.slave.core.service.contract.ContractQuery;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/6/22
 */
@Slf4j
@Service
public class ContractQueryServiceImpl implements ContractQueryService {
    private static final int CONTRACT_ADDRESS_LENGTH = 20;
    private static final String CONTRACT_ADDRESS_PATTERN = "^0x[0-9a-zA-Z]{40}";

    @Autowired
    private StandardSmartContract standardSmartContract;
    @Autowired
    private ContractQuery contractQuery;

    @Override
    public Object query(String address, String methodName, Object... args) {
        return standardSmartContract.executeQuery(address, methodName, args);
    }

    @Override
    public List<?> query(byte[] contractAddress, String methodSignature, Object... args) {
        if (contractAddress.length != CONTRACT_ADDRESS_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Contract address length must be %d bytes", CONTRACT_ADDRESS_LENGTH));
        }

        if (Hex.toHexString(contractAddress).matches(CONTRACT_ADDRESS_PATTERN)) {
            throw new IllegalArgumentException("Contract address is incorrect");
        }

        if (StringUtils.isEmpty(methodSignature)) {

            throw new IllegalArgumentException("Contract method signature cannot be empty");
        }

        return contractQuery.executeQuery(contractAddress, methodSignature, args);
    }
}
