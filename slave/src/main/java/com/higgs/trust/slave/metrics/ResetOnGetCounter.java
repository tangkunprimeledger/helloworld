package com.higgs.trust.slave.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author duhongming
 * @date 2018/12/28
 */
public class ResetOnGetCounter implements WindowCounter {
    private final LongAdder count;

    public ResetOnGetCounter() {
        count = new LongAdder();
    }

    @Override
    public long getCount() {
        long value = count.sum();
        count.add(-value);
        return value;
    }

    @Override
    public void inc(long n) {
        count.add(n);
    }
}
