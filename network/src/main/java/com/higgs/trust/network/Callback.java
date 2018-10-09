package com.higgs.trust.network;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author duhongming
 * @date 2018/9/3
 */
public class Callback {
    private final String action;
    private final long timeout;
    private final CompletableFuture<byte[]> future;
    private final long time = System.currentTimeMillis();

    Callback(String action, Duration timeout, CompletableFuture<byte[]> future) {
        this.action = action;
        this.timeout = timeout != null ? timeout.toMillis() : 0;
        this.future = future;
    }

    public long time() {
        return time;
    }

    public long timeout() {
        return timeout;
    }

    public void complete(byte[] value) {
        future.complete(value);
    }

    public void completeExceptionally(Throwable error) {
        future.completeExceptionally(error);
    }
}
