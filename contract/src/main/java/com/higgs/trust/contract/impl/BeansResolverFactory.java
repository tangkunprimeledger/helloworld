package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;

import java.util.Map;

public class BeansResolverFactory implements ResolverFactory, Resolver {

    private Map<String, Object> registedVariables;

    public BeansResolverFactory(Map<String, Object> variables) {
        this.registedVariables = variables;
    }

    @Override public boolean containsKey(Object key) {
        return registedVariables.containsKey(key);
    }

    @Override public Object get(Object key) {
        return registedVariables.get(key);
    }

    @Override public Resolver createResolver(ExecuteContext context) {
        return this;
    }
}
