package com.higgs.trust.consensus.p2pvalid.core;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author cwy
 */
@Setter
@Getter
@ToString
public abstract class ValidCommand<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1L;

    private T t;

    private HashFunction function = Hashing.sha256();

    public ValidCommand() {
    }

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

    public String getMessageDigestHash(){
        return function.hashString(messageDigest(), Charsets.UTF_8).toString();
    }


}
