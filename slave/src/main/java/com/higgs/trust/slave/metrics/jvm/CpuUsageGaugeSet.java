package com.higgs.trust.slave.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/12/28
 */
public class CpuUsageGaugeSet implements MetricSet {


    private final OperatingSystemMXBean  mxBean;

    public CpuUsageGaugeSet() {
        this((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
    }

    public CpuUsageGaugeSet(OperatingSystemMXBean mxBean) {
        this.mxBean = mxBean;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();
        gauges.put("cpu.arch", (Gauge<Object>) () -> mxBean.getArch());
        gauges.put("cpu.processors", (Gauge<Object>) () -> mxBean.getAvailableProcessors());
        gauges.put("cpu.process.load", (Gauge<Object>) () -> mxBean.getProcessCpuLoad());
        gauges.put("cpu.system.load", (Gauge<Object>) () -> mxBean.getSystemCpuLoad());
        return Collections.unmodifiableMap(gauges);
    }
}
