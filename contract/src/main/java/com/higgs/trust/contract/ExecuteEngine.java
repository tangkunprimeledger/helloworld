package com.higgs.trust.contract;

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
