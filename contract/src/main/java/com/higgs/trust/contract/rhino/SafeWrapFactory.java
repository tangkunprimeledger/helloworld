package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.AccessDeniedException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * @author duhongming
 * @date 2018/6/8
 */
public class SafeWrapFactory extends WrapFactory {

    @Override
    public Scriptable wrapJavaClass(Context cx, Scriptable scope, Class<?> javaClass) {
        String name = javaClass.getName();
        if (name.startsWith("java.io") || name.startsWith("java.lang.reflect")) {
            throw new AccessDeniedException(name);
        }
        return new NativeJavaClass(scope, javaClass);
    }

    @Override
    public Scriptable wrapAsJavaObject(final Context cx, final Scriptable scope, final Object javaObject, final Class<?> staticType) {
        return new SafeNativeJavaObject(scope, javaObject, staticType);
    }
}