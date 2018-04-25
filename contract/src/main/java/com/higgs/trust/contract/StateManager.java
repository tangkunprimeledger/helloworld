package com.higgs.trust.contract;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class StateManager {
    private Map<String, Object> state;
    private Map<String, Object> oldState;

    public StateManager() {
        state = new HashMap<>(8);
    }

    public StateManager(Map<String, Object> state) {
        this.state = state == null ? new HashMap<>(8) : state;
        if (state != null) {
            this.oldState = JSON.parseObject(JSON.toJSONString(state));
        }
    }

    private int toInt(Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String)value);
        } else if (value instanceof Number) {
            return ((Number)value).intValue();
        } else {
            return ((Integer) value).intValue();
        }
    }

    public StateManager put(String name, Object value) {
        this.state.put(name, JSON.parse(JSON.toJSONString(value)));
        return this;
    }

    public Object get(String name) {
        Object obj = this.state.get(name);
        if (obj == null) {
            return null;
        }
        return JSON.parse(JSON.toJSONString(obj));
    }

    public int getInt(String name) {
        Object value = get(name);
        return toInt(value);
    }

    public Map<String, Object> getState() {
        return this.state;
    }

    public Map<String, Object> getOldState() {
        return this.oldState;
    }
}
