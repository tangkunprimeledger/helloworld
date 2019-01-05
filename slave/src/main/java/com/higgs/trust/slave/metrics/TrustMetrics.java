package com.higgs.trust.slave.metrics;


import com.codahale.metrics.*;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.slave.metrics.jvm.CpuUsageGaugeSet;
import com.higgs.trust.slave.metrics.jvm.MemoryUsageGaugeSet;
import com.higgs.trust.slave.metrics.jvm.ThreadStatesGaugeSet;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author duhongming
 * @date 2018/12/27
 */
public class TrustMetrics {

    private Map<String, Object> reportedMetrics;

    private MetricRegistry register = new MetricRegistry();
    private MetricsReporter reporter;

    private final TransactionsMetricSet transactionsMetricSet;
    private final BlockMetricSet blockMetricSet;

    private static TrustMetrics defaultTrustMetrics = new TrustMetrics();

    public static TrustMetrics getDefault() {
        return defaultTrustMetrics;
    }

    private TrustMetrics() {
        transactionsMetricSet = new TransactionsMetricSet();
        blockMetricSet = new BlockMetricSet();
        registerMetrics();
        NetworkManage.installTrafficReporter(new NetworkTrafficReporter(this));
        reporter = new MetricsReporter(this::report);
    }

    private void registerMetrics() {
        register.register("memory", new MemoryUsageGaugeSet(new ArrayList<>(0)));
        register.register("cpu", new CpuUsageGaugeSet());
        register.register("thread", new ThreadStatesGaugeSet());
    }

    public BlockMetricSet block() {
        return blockMetricSet;
    }

    public TransactionsMetricSet transactions() {
        return transactionsMetricSet;
    }

    public MetricsReporter getReporter() {
        return reporter;
    }

    public Map<String, Object> getReportedMetrics() {
        return reportedMetrics;
    }

    public void startReport() {
        reporter.start(1, TimeUnit.SECONDS);
    }

    public void increment(String metricName) {
        increment(metricName, 1);
    }

    public void increment(String metricName, long n) {
        register.counter(metricName).inc(n);
    }

    public void mark(String metricName) {
        mark(metricName, 1);
    }

    public void mark(String metricName, long n) {
        register.meter(metricName).mark(n);
    }

    public void update(String metricName, long value) {
        register.histogram(metricName).update(value);
    }

    public MetricRegistry getRegister() {
        return register;
    }

    private void report() {
        Map map = new TreeMap<>();
        map.put("timestamp", System.currentTimeMillis());
        report(map, register.getMetrics());
        report(map, blockMetricSet.getMetrics());
        report(map, transactionsMetricSet.getMetrics());

        reportedMetrics = map;
    }

    private void report(Map map, Map<String, Metric> metrics) {
        metrics.forEach((name, metric) -> {
            if (metric instanceof WindowMeter) {
                WindowMeter windowMeter = (WindowMeter) metric;
                map.put(name +".count.total", windowMeter.getCount());
                map.put(name +".count.second", windowMeter.getCountAndReset());
                map.put(name +".count.second.max", windowMeter.getMaxCountPerWindow());
                map.put(name +".count.mean-rate", windowMeter.getMeanRate());
            } else if (metric instanceof Meter) {
                Meter meter = (Meter) metric;
                map.put(name + ".count", meter.getCount());
                map.put(name + ".mean-rate", meter.getMeanRate());
                map.put(name + ".one-minute-rate", meter.getOneMinuteRate());
            } else if (metric instanceof Counter) {
                Counter counter = (Counter) metric;
                map.put(name + ".count", counter.getCount());
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                map.put(name, gauge.getValue());
            } else if (metric instanceof Timer) {
                Timer timer = (Timer) metric;
                map.put(name + "." + "timer.count", timer.getCount());
                Snapshot snapshot = timer.getSnapshot();
                map.put(name + "." + "timer.max", snapshot.getMax());
                map.put(name + "." + "timer.min", snapshot.getMin());
                map.put(name + "." + "timer.mean", snapshot.getMean());
                map.put(name + "." + "timer.median", snapshot.getMedian());
            }
        });
    }
}
