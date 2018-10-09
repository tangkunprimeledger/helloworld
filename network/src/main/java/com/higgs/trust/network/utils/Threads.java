package com.higgs.trust.network.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ThreadFactory;

/**
 * @author duhongming
 * @date 2018/9/5
 */
public class Threads {
    public static ThreadFactory namedThreads(String pattern, Logger log) {
        return new ThreadFactoryBuilder()
                .setNameFormat(pattern)
                .setThreadFactory(r -> new Thread(r))
                .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception on " + t.getName(), e))
                .build();
    }
}
