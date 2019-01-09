package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.metrics.TrustMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author duhongming
 * @date 2018/12/27
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @GetMapping("/metrics")
    public Object getMetrics() {
        return TrustMetrics.getDefault().getReportedMetrics();
    }
}
