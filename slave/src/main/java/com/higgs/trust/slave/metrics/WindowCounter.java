package com.higgs.trust.slave.metrics;

/**
 * @author duhongming
 * @date 2018/12/28
 */
public interface WindowCounter {
    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    long getCount();

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void inc(long n);

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be decreased
     */
    default void dec(long n) {
        inc(-n);
    }

    /**
     * Increment the counter by one.
     */
    default void inc() {
        inc(1);
    }

    /**
     * Decrement the counter by one.
     */
    default void dec() {
        dec(-1);
    }
}
