package com.higgs.trust.slave.metrics;

import com.codahale.metrics.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2019/1/3
 */
public class TransactionsMetricSet {
    private final Timer timer;
    private final Histogram histogram;

    private final ValuedGauge<Long> totalCountGauge;
    private final ValuedGauge<Long> perSecondCountGauge;

    private long lastRecordCount;
    private long currentTotalCount;


    public TransactionsMetricSet() {
        timer = new Timer();
        histogram = new Histogram(new UniformReservoir());

        totalCountGauge = new ValuedGauge<>();
        perSecondCountGauge = new ValuedGauge<>();
    }

    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<>();

        currentTotalCount = timer.getCount();
        totalCountGauge.setValue(currentTotalCount);
        long perSecondCount = currentTotalCount - lastRecordCount;
        lastRecordCount = currentTotalCount;
        perSecondCountGauge.setValue(perSecondCount);
        histogram.update(perSecondCount);

        metrics.put("transactions.second.current", perSecondCountGauge);
        metrics.put("transactions.total", totalCountGauge);

        Snapshot snapshot = timer.getSnapshot();
        metrics.put("transactions.timer.max", new ValuedGauge<>(snapshot.getMax()));
        metrics.put("transactions.timer.mean", new ValuedGauge<>(snapshot.getMean()));

        snapshot = histogram.getSnapshot();
        metrics.put("transactions.second.max", new ValuedGauge<>(snapshot.getMax()));
        metrics.put("transactions.second.mean", new ValuedGauge<>(snapshot.getMean()));

        return metrics;
    }

    public Timer.Context time() {
        return timer.time();
    }

    public void time(Runnable run) {
        timer.time(run);
    }
}
