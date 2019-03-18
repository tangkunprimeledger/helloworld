package com.higgs.trust.contract.rhino.function;

import com.higgs.trust.contract.rhino.types.BigDecimalWrap;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * @author duhongming
 * @date 2018/6/6
 */
@Slf4j
public class PrintNativeFunction extends BaseFunction {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        Object o = args[0];
        String s = o instanceof Number || o instanceof BigDecimalWrap ? o.toString() : Context.toString(o);
        System.out.println(s);
        log.info(s);
        return Undefined.instance;
    }
}
