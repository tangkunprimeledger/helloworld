package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.ContractQueryService;
import com.higgs.trust.slave.core.service.contract.ContractQuery;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author duhongming
 * @date 2018/6/22
 */
@Slf4j
@Service
public class ContractQueryServiceImpl implements ContractQueryService {
    private static final int CONTRACT_ADDRESS_LENGTH = 20;
    private static final Pattern CONTRACT_ADDRESS_PATTERN = Pattern.compile("^[0-9a-fA-F]{40}$");

    @Autowired
    private StandardSmartContract standardSmartContract;
    @Autowired
    private ContractQuery contractQuery;

    @Override
    public Object query(String address, String methodName, Object... args) {
        return standardSmartContract.executeQuery(address, methodName, args);
    }

    @Override
    public List<?> query2(int blockHeight, String contractAddressAsString, String methodSignature, Object... methodInputArgs) {
        if (!CONTRACT_ADDRESS_PATTERN.matcher(contractAddressAsString).matches()) {
            throw new IllegalArgumentException("Contract address must be hex string of 40 characters");
        }

        byte[] contractAddress = Hex.decode(contractAddressAsString);
        if (contractAddress.length != CONTRACT_ADDRESS_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Contract address length must be %d bytes", CONTRACT_ADDRESS_LENGTH));
        }

        ParameterChecker parameterChecker =
                new ParameterChecker().withMethodSignature(methodSignature).withMethodInputArgs(methodInputArgs);
        parameterChecker.check();

        return contractQuery.executeQuery(blockHeight, contractAddress, methodSignature, methodInputArgs);
    }

    private static class ParameterChecker {
        private static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("^\\(.*?\\) ??\\w+?\\((.*?)\\)$");

        private String methodSignature;
        private Object[] methodInputArgs;

        public void check() {
            Matcher matcher = METHOD_SIGNATURE_PATTERN.matcher(methodSignature);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Contract method signature is illegal");
            }

            String inputParams = matcher.group(1).trim();
            int count = inputParams.split(",").length;
            boolean paramAdapted = StringUtils.isEmpty(inputParams) && methodInputArgs.length == 0 || methodInputArgs.length == count;
            if (!paramAdapted) {
                throw new IllegalArgumentException("Input parameter is not equal to method signature");
            }
        }

        public ParameterChecker withMethodSignature(String methodSignature) {
            this.methodSignature = methodSignature;
            return this;
        }

        public ParameterChecker withMethodInputArgs(Object[] methodInputArgs) {
            this.methodInputArgs = methodInputArgs;
            return this;
        }
    }
}
