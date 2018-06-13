package com.higgs.trust.contract.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * @author duhongming
 * @date 2018/6/7
 */
public class TrustContext extends Context {

    protected int quota;

    public TrustContext(ContextFactory factory) {
        super(factory);
    }
}
