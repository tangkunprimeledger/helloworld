package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/18
 *
 */
public class PackageSchedulerTest extends BaseTest{

    @Autowired
    private PackageScheduler packageScheduler;

    @Test public void createPackage() {
        packageScheduler.createPackage();
    }

    @Test public void submitPackage() {
        packageScheduler.submitPackage();
    }

    @Test public void processPackage() {
        packageScheduler.processPackage();
    }
}