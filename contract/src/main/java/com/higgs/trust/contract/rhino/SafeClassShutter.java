package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.ExecuteConfig;
import com.higgs.trust.contract.StateManager;
import com.higgs.trust.contract.rhino.function.MathFuncs;
import com.higgs.trust.contract.rhino.types.BigDecimalWrap;
import org.mozilla.javascript.ClassShutter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author duhongming
 * @date 2018/6/8
 */
public class SafeClassShutter implements ClassShutter {

    public final Set<String> allowedClasses;
    private static final Set<String> buildInClasses;

    static {
        buildInClasses = new HashSet<>();
        buildInClasses.add("java.lang.Long");
        buildInClasses.add("java.lang.Integer");
        buildInClasses.add("java.math.BigDecimal");
        buildInClasses.add("java.math.BigInteger");

        buildInClasses.add("java.lang.String");
        buildInClasses.add("java.util.ArrayList");
        buildInClasses.add("java.util.HashMap");
        buildInClasses.add("java.util.LinkedList");
        buildInClasses.add("java.util.LinkedList");
        buildInClasses.add("java.util.TreeSet");

        buildInClasses.add("com.alibaba.fastjson.JSONArray");
        buildInClasses.add("com.alibaba.fastjson.JSONObject");

        buildInClasses.add(StateManager.class.getName());
        buildInClasses.add(MathFuncs.class.getName());
        buildInClasses.add(BigDecimalWrap.class.getName());

    }

    public SafeClassShutter(final Set<String> allowedClasses) {
        this.allowedClasses = allowedClasses == null ? new HashSet<>() : allowedClasses;
    }

    @Override
    public boolean visibleToScripts(String fullClassName) {
        if (ExecuteConfig.DEBUG) {
            return true;
        }

        boolean allowed = buildInClasses.contains(fullClassName) || this.allowedClasses.contains(fullClassName);
        return allowed;
    }
}
