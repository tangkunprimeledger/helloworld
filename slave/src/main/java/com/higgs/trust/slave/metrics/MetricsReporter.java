package com.higgs.trust.slave.metrics;

import com.higgs.trust.network.utils.Threads;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author duhongming
 * @date 2018/12/27
 */
public class MetricsReporter {

    private final ScheduledExecutorService executor;
    private final Runnable reporter;

    public MetricsReporter(Runnable reporter) {
        this.reporter = reporter;
        executor = new ScheduledThreadPoolExecutor(1, Threads.namedThreads("MetricsReporter"));
    }

    public void start(long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(this::report, period, period, unit);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }

    public void shutdown() {
        executor.shutdown();
    }

    private void report() {
        try {
            reporter.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
