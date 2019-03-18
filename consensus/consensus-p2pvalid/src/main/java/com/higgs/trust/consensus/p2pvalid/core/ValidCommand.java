package com.higgs.trust.consensus.p2pvalid.core;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cwy
 */
@Slf4j @Setter @Getter @ToString public abstract class ValidCommand<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1L;
    private static ConcurrentHashMap cmdTypeMap = new ConcurrentHashMap();

    private T t;

    private long view;

    private String cmdName;

    public ValidCommand() {
    }

    public ValidCommand(T t, long view) {
        this.t = t;
        this.view = view;
    }

    public T get() {
        return t;
    }

    public String getCmdName() {
        return this.getClass().getSimpleName();
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
