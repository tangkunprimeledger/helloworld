package com.higgs.trust.contract.rhino.function;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * @author duhongming
 * @date 2018/6/8
 */
public class SafeEvalFunction extends BaseFunction {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        // do nothing for eval
        // System.out.println(String.format("eval: %s", String.valueOf(args[0])));
        return Undefined.instance;
    }
}
