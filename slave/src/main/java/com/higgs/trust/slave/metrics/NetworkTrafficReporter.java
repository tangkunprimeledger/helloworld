package com.higgs.trust.slave.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.higgs.trust.network.TrafficReporter;

/**
 * @author duhongming
 * @date 2018/12/27
 */
public class NetworkTrafficReporter implements TrafficReporter {

    private long maxInboundPerSecond;
    private long maxOutboundPerSecond;

    private long lastInbound;
    private long lastOutbound;

    private Counter inboundCounter;
    private Counter outboundCounter;

    public NetworkTrafficReporter(TrustMetrics trustMetrics) {
        this.inboundCounter = new Counter();
        this.outboundCounter = new Counter();
        trustMetrics.getRegister().register("network.inbound.bytes", this.inboundCounter);
        trustMetrics.getRegister().register("network.outbound.bytes", this.outboundCounter);
        trustMetrics.getRegister().register("network.inbound.bytes.max-per-second", (Gauge<Long>) () -> {
            long val = inboundCounter.getCount();
            long s = val - lastInbound;
            lastInbound = val;
            maxInboundPerSecond = Math.max(maxInboundPerSecond, s);
            return s;
        });
        trustMetrics.getRegister().register("network.outbound.bytes.max-per-second", (Gauge<Long>) () -> {
            long val = outboundCounter.getCount();
            long s = val - lastOutbound;
            lastOutbound = val;
            maxOutboundPerSecond = Math.max(maxOutboundPerSecond, s);
            return s;
        });
    }

    @Override
    public void inbound(long bytes) {
        inboundCounter.inc(bytes);
    }

    @Override
    public void outbound(long bytes) {
        outboundCounter.inc(bytes);
    }
}
