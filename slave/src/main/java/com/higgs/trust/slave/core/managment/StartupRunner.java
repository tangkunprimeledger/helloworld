/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment;

import com.higgs.trust.slave.core.service.failover.SelfCheckingService;
import com.higgs.trust.slave.core.service.failover.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/4/24
 */
@Component public class StartupRunner implements CommandLineRunner {

    @Autowired private SelfCheckingService selfCheckingService;
    @Autowired private SyncService syncService;

    @Override public void run(String... strings) {
        boolean check = selfCheckingService.check();
        if (check) {
            syncService.sync();
        }
    }
}