package com.higgs.trust.contract;

import java.util.Set;

/**
 * smart contract runtime context data
 *
 * @author duhongming
 * @date 2018-04-17
 */
public interface ExecuteContextData {

    public static final String KEY_CONTRACT_INSTANCE_ID = "CONTRACT_INSTANCE_ID";
    public static final String KEY_CONTRACT_ADDRESS = "CONTRACT_ADDRESS";

    Object get(String key);
    ExecuteContextData put(String key, Object object);
    Set<String> keySet();
}
