package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.ExecuteConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/6/8
 */
public class SafeNativeJavaObject extends NativeJavaObject {

    public SafeNativeJavaObject(final Scriptable scope, final Object javaObject, final Class<?> staticType) {
        super(scope, javaObject, staticType);
    }

    @Override
    public Object get(final String name, final Scriptable start) {
        boolean _equals = "getClass".equals(name);
        if (_equals && !ExecuteConfig.DEBUG) {
            return Scriptable.NOT_FOUND;
        }

        if (name.equals("length")) {
            if (unwrap() instanceof List) {
                List list = (List) unwrap();
                return list.size();
            }
        }
        Object val = unwrap();
        if (val instanceof Map) {
            Map mapVal = (Map) val;
            return mapVal.containsKey(name) ? mapVal.get(name) : super.get(name, start);
        }

        return super.get(name, start);
    }

    @Override
    public Object get(final int index, final Scriptable start) {
        Object obj = unwrap();
        if (obj instanceof List) {
            return  ((List) obj).get(index);
        }
        return super.get(index, start);
    }
}
