package com.higgs.trust.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.HashMap;
import java.util.Map;

/**
 *  contract state manager
 *  @author duhongming
 */
public class StateManager {
    private Map<String, Object> state;
    private Map<String, Object> oldState;
    public final static int JSON_GENERATE_FEATURES;

    static {
        int features = 0;
        features = features | SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.SortField.getMask();
        features |= SerializerFeature.MapSortField.getMask();
        JSON_GENERATE_FEATURES = features;
    }

    public StateManager() {
        state = new HashMap<>(8);
    }

    public StateManager(Map<String, Object> state) {
        this(state, true);
    }

    public StateManager(Map<String, Object> state, boolean backup) {
        this.state = state == null ? new HashMap<>(8) : state;
        if (state != null && backup) {
            //this.oldState = JSON.parseObject(JSON.toJSONString(state, JSON_GENERATE_FEATURES), HashMap.class);
            this.oldState = new HashMap<>(this.state.size());
            this.state.forEach((key, value) -> this.oldState.put(key, value));
        }
    }

    private int toInt(Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return ((Integer) value).intValue();
        }
    }

    public StateManager put(String name, Object value) {
        this.state.put(name, JSON.parse(JSON.toJSONString(value, JSON_GENERATE_FEATURES)));
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
