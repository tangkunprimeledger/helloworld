package com.higgs.trust.consensus.p2pvalid.core;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.consensus.common.SHAUtils;
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
        return SHAUtils.sha256(messageDigest());
    }


}
