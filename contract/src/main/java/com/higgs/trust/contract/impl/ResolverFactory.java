package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;

public interface ResolverFactory {
    Resolver createResolver(ExecuteContext context);
}
