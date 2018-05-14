package com.higgs.trust.rs.custom.biz.scheduler.identity;

import com.higgs.trust.IntegrateBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/5/14 19:23
 */
public class StorageIdentityTaskTest extends IntegrateBaseTest {

    @Autowired StorageIdentityTask storageIdentityTask;

    @Test public void testProcess() throws Exception {
        storageIdentityTask.process();
    }
}