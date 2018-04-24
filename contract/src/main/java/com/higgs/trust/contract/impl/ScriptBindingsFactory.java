package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.List;

public class ScriptBindingsFactory {
    protected List<ResolverFactory> resolverFactories;

    public ScriptBindingsFactory(List<ResolverFactory> resolverFactories) {
        this.resolverFactories = resolverFactories;
    }

    public Bindings createBindings(ExecuteContext context) {
        return new ScriptBindings(createResolvers(context), context);
    }

    protected List<Resolver> createResolvers(ExecuteContext context) {
        List<Resolver> scriptResolvers = new ArrayList<Resolver>();
        for (ResolverFactory scriptResolverFactory : resolverFactories) {
            Resolver resolver = scriptResolverFactory.createResolver(context);
            if (resolver != null) {
                scriptResolvers.add(resolver);
            }
        }
        return scriptResolvers;
    }

    public List<ResolverFactory> getResolverFactories() {
        return resolverFactories;
    }

    public void setResolverFactories(List<ResolverFactory> resolverFactories) {
        this.resolverFactories = resolverFactories;
    }
}
