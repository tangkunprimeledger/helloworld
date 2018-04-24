package com.higgs.trust.consensus.p2pvalid.core;

import java.io.Serializable;

/**
 * @author cwy
 */
public abstract class ValidCommand<T> implements Serializable {

    private static final long serialVersionUID = -1L;

    private T t;

    public ValidCommand(){}

    public ValidCommand(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public Class<?> type() {
        return t.getClass();
    }

    public abstract String messageDigest();

    @Override
    public String toString() {
        return "ValidCommand{" +
                "t=" + t +
                '}';
    }
}
