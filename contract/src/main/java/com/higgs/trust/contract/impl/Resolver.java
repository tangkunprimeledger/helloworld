package com.higgs.trust.contract.impl;

public interface Resolver {

    boolean containsKey(Object key);

    Object get(Object key);
}
