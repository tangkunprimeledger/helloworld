package com.higgs.trust.slave.common.util.asynctosync;

public interface BlockingMap<V> {
    void put(String key, V o) throws InterruptedException;

    V take(String key) throws InterruptedException;

    V poll(String key, long timeout) throws InterruptedException;
}
