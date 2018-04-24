package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.*;

public class ScriptBindings implements Bindings {
    /**
     * This list contains the keywords for Javascript.
     */
    protected static final Set<String> UNSTORED_KEYS = new HashSet<String>(
        Arrays.asList("out", "out:print", "lang:import", "context", "elcontext", "print", "println", "nashorn.global"));

    protected List<Resolver> scriptResolvers;
    protected Bindings defaultBindings;
    protected ExecuteContext context;

    public ScriptBindings(List<Resolver> scriptResolvers, ExecuteContext context) {
        this.scriptResolvers = scriptResolvers;
        this.context = context;
        this.defaultBindings = new SimpleBindings();
    }

    @Override public boolean containsKey(Object key) {


        for (Resolver scriptResolver : scriptResolvers) {
            if (scriptResolver.containsKey(key)) {
                return true;
            }
        }
        return defaultBindings.containsKey(key);
    }

    @Override public Object get(Object key) {


        for (Resolver scriptResolver : scriptResolvers) {
            if (scriptResolver.containsKey(key)) {
                return scriptResolver.get(key);
            }
        }
        return defaultBindings.get(key);
    }

    @Override public Object put(String name, Object value) {
        return defaultBindings.put(name, value);
    }

    @Override public void putAll(Map<? extends String, ? extends Object> toMerge) {
        throw new UnsupportedOperationException();
    }

    @Override public Object remove(Object key) {
        if (UNSTORED_KEYS.contains(key)) {
            return null;
        }
        return defaultBindings.remove(key);
    }

    @Override public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return defaultBindings.keySet();
    }

    @Override
    public Collection<Object> values() {
        return defaultBindings.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return defaultBindings.entrySet();
    }

    @Override public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return defaultBindings.size();
    }

    @Override public boolean isEmpty() {
        return defaultBindings.isEmpty();
    }

}
