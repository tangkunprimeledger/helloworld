package com.higgs.trust.slave.api;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/6/22
 */
public interface ContractQueryService {
    /**
     * Queries contract state.
     *
     * @param address
     * @param methodName
     * @param args
     * @return
     */
    Object query(String address, String methodName, Object... args);

    /**
     * Queries contract state.
     *
     * @param blockHeight     block height
     * @param contractAddress contract address
     * @param methodSignature method signature written with target language
     * @param methodInputArgs actual parameters
     * @return result returned by contract invocation
     */
    List<?> query2(long blockHeight, String contractAddress, String methodSignature, Object... methodInputArgs);

    /**
     * Queries contract state.
     *
     * @param blockHeight     block height
     * @param contractAddress contract address
     * @param methodSignature method signature written with target language
     * @param methodInputArgs actual parameters
     * @return result returned by contract invocation
     */
    default List<?> query2(Long blockHeight, String contractAddress, String methodSignature, Object... methodInputArgs) {
        long height = -1;
        if (blockHeight != null) {
            height = blockHeight;
        }
        
        return query2(height, contractAddress, methodSignature, methodInputArgs);
    }

    /**
     * Queries contract state.
     *
     * @param contractAddress contract address
     * @param methodSignature method signature written with target language
     * @param methodInputArgs actual parameters
     * @return result returned by contract invocation
     */
    default List<?> query2(String contractAddress, String methodSignature, Object... methodInputArgs) {
        return query2(-1, contractAddress, methodSignature, methodInputArgs);
    }
}
