package com.higgs.trust.contract.rhino;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

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
        if (_equals) {
            return Scriptable.NOT_FOUND;
        }
        return super.get(name, start);
    }
}
