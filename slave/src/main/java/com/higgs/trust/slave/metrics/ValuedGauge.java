package com.higgs.trust.slave.metrics;

import com.codahale.metrics.Gauge;

/**
 * @author duhongming
 * @date 2019/1/3
 */
public class ValuedGauge<T> implements Gauge<T> {

    private T value;

    public ValuedGauge() {

    }

    public ValuedGauge(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
