package com.higgs.trust.slave.metrics;

import com.codahale.metrics.Meter;

/**
 * @author duhongming
 * @date 2018/12/28
 */
public class WindowMeter extends Meter {

    private ResetOnGetCounter winCounter;
    private double maxCountPerWindow;

    public WindowMeter() {
        winCounter = new ResetOnGetCounter();
    }

    @Override
    public void mark(long n) {
        winCounter.inc(n);
        super.mark(n);
    }

    public double getCountAndReset() {
        double count = winCounter.getCount();
        maxCountPerWindow = Math.max(maxCountPerWindow, count);
        return count;
    }

    public double getMaxCountPerWindow() {
        return maxCountPerWindow;
    }
}
