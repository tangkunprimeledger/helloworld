package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.ExecuteConfig;
import org.mozilla.javascript.ClassShutter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

        buildInClasses.add("com.higgs.trust.contract.StateManager");
        buildInClasses.add("com.alibaba.fastjson.JSONArray");
        buildInClasses.add("com.alibaba.fastjson.JSONObject");

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
