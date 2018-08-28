package com.higgs.trust.common.utils;

import org.rocksdb.WriteBatch;

/**
 * ThreadLocal utils
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<WriteBatch> writeBatch = new ThreadLocal<>();

    public static void putWriteBatch (WriteBatch batch) {
        if (null != getWriteBatch()) {
            clearWriteBatch();
        }
        writeBatch.set(batch);
    }

    public static void clearWriteBatch() {
        writeBatch.remove();
    }

    public static WriteBatch getWriteBatch() {
        return writeBatch.get();
    }

}
