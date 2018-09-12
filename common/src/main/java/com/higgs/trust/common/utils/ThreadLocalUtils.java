package com.higgs.trust.common.utils;

import org.rocksdb.Transaction;

/**
 * ThreadLocal utils
 * @author tangfashuang
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<Transaction> rocksTx = new ThreadLocal<>();

    public static void putRocksTx(Transaction tx) {
        if (null != getRocksTx()) {
            clearRocksTx();
        }
        rocksTx.set(tx);
    }

    public static void clearRocksTx() {
        rocksTx.remove();
    }

    public static Transaction getRocksTx() {
        return rocksTx.get();
    }
}
