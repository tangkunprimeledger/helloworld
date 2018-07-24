package com.higgs.trust.contract.rhino.types;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/6/6
 */
public class NativeJavaMap extends NativeJavaObject {

    private Map<String, Object> map;

    public NativeJavaMap(Map<String, Object> map) {
        this.map = map;
        if (map == null) {
            this.map = new HashMap<>(4);
        }
    }

    @Override
    public Object unwrap() {
        return map;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return map.containsKey(name);
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object result = this.map.get(name);
        if (result instanceof BigInteger) {
            return new BigDecimalWrap((BigInteger) result);
        }
        return result;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        map.put(name, value);
    }

    @Override
    public Object[] getIds() {
        return map.keySet().toArray(new Object[map.size()]);
    }

}
