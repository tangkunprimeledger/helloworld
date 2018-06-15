package com.higgs.trust.consensus.p2pvalid.core;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author cwy
 */
@Slf4j @Setter @Getter @ToString public abstract class ValidCommand<T extends Serializable> implements Serializable {

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

    public String getMessageDigestHash() {
        String messageDigest = messageDigest();
        String digest = this.getClass().getName().concat("_").concat(messageDigest);
        log.trace("message digest:{}, hash digest:{}", messageDigest, digest);
        return Hashing.sha256().hashString(digest, Charsets.UTF_8).toString();
    }

}
