package com.higgs.trust.consensus.p2pvalid.core;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author cwy
 */
@Setter @Getter @ToString public abstract class ResponseCommand<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1L;

    private T t;
    private String cmdName;

    public ResponseCommand() {
        this.setCmdName(this.getClass().getSimpleName());
    }

    public ResponseCommand(T t) {
        this();
        this.t = t;
    }

    public T get() {
        return t;
    }

    public Class<?> type() {
        return t.getClass();
    }

    public abstract String messageDigest();

    public String getMessageDigestHash() {
        return Hashing.sha256()
            .hashString(this.getClass().getName().concat("_").concat(messageDigest()), Charsets.UTF_8).toString();
    }

}
