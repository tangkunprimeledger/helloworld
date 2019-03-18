package com.higgs.trust.network.eventbus;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author duhongming
 * @date 2018/8/16
 */
public class AsyncEventBus extends EventBus {

    public AsyncEventBus(ThreadPoolExecutor executor) {
        this(null, executor);
    }
    public AsyncEventBus(EventExceptionHandler exceptionHandler, ThreadPoolExecutor executor) {
        this("default-async", exceptionHandler, executor);
    }

    public AsyncEventBus(String busName, EventExceptionHandler exceptionHandler, ThreadPoolExecutor executor) {
        super(busName, exceptionHandler, executor);
    }
}
