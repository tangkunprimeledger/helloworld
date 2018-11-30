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
     * 查询合约
     *
     * @param contractAddress
     * @param methodSignature
     * @param args
     * @return
     */
    List<?> query(byte[] contractAddress, String methodSignature, Object... args);
}
