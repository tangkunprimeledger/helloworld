package com.higgs.trust.contract.rhino;

import org.mozilla.javascript.ClassShutter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author duhongming
 * @date 2018/6/8
 */
public class SafeClassShutter implements ClassShutter {

    public final Set<String> allowedClasses;

    public SafeClassShutter(final Set<String> allowedClasses) {
        this.allowedClasses = allowedClasses == null ? new HashSet<>() : allowedClasses;
        this.allowedClasses.add("java.lang.Long");
        this.allowedClasses.add("java.lang.String");
        this.allowedClasses.add("com.higgs.trust.contract.StateManager");
        this.allowedClasses.add("com.alibaba.fastjson.JSONArray");
        this.allowedClasses.add("com.alibaba.fastjson.JSONObject");
    }

    @Override
    public boolean visibleToScripts(String fullClassName) {
        boolean allowed = this.allowedClasses.contains(fullClassName);
        return allowed;
    }
}
