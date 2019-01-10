package com.higgs.trust.rs.core.api;

import java.util.List;

/**
 * @author: lingchao
 * @datetime:2019-01-05 23:28
 **/
public interface ContractV2QueryService {
    /**
     * query state for distribute RS and local RS with slave
     * @param blockHeight
     * @param contractAddress
     * @param methodSignature
     * @param methodInputArgs
     * @return
     */
    List<?> query(Long blockHeight, String contractAddress, String methodSignature, Object... methodInputArgs);
}
