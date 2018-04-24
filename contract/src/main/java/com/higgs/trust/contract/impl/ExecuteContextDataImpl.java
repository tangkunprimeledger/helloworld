package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContextData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * smart contract runtime context data
 */
public class ExecuteContextDataImpl implements ExecuteContextData {

    private HashMap<String, Object> data;

    public ExecuteContextDataImpl() {
        data = new HashMap<>();
    }

    public ExecuteContextDataImpl(Map<String, Object> data) {
        this();
        if (null != data) {
            data.forEach((key, value) -> {
                this.put(key, value);
            });
        }
    }

    @Override public Object get(String key) {
        return data.get(key);
    }

    @Override public ExecuteContextData put(String key, Object object) {
        data.put(key, object);
        return this;
    }

    @Override public Set<String> keySet() {
        return data.keySet();
    }

    public static ExecuteContextDataImpl newContextData(Map<String, Object> data) {
        ExecuteContextDataImpl contextData = new ExecuteContextDataImpl(data);
        return contextData;
    }

    public static ExecuteContextDataImpl newContextData() {
        return newContextData(null);
    }
}
