package com.higgs.trust.contract;

/**
 * @author duhongming
 * @date 2018/04/25
 */
public interface ExecuteEngine {

    public static final String JAVASCRIPT = "javascript";

    /**
     * execute smart contract by give method
     *
     * @param methodName
     * @param bizArgs
     * @return
     */
    Object execute(String methodName, Object... bizArgs);
}
