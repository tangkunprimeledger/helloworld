package com.higgs.trust.slave.api;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/6/22
 */
public interface ContractQueryService {
    /**
     * 查询合约
     *
     * @param address
     * @param methodName
     * @param args
     * @return
     */
    Object query(String address, String methodName, Object... args);

    /**
     * Queries contract.
     *
     * @param contractAddress contract address
     * @param methodSignature method signature written with target language
     * @param methodInputArgs actual parameters
     * @return result returned by contract invocation
     */
    List<?> query2(String contractAddress, String methodSignature, Object... methodInputArgs);
}
