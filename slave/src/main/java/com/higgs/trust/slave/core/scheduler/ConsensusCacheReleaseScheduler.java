/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.core.service.consensus.cluster.ClusterServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author suimi
 * @date 2018/4/25
 */
@Service @Slf4j public class ConsensusCacheReleaseScheduler {

    @Autowired private ClusterServiceImpl clusterService;

    @Scheduled(fixedDelay = 30000) public void release() {
        clusterService.releaseResult();
    }
}

