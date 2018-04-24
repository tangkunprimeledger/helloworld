package com.higgs.trust.slave.core.service.consensus.log;

import com.google.common.collect.Lists;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.vo.PackageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class LogReplicateHandlerImplTest extends BaseTest {
    @Autowired LogReplicateHandler logReplicateHandler;

    @Test public void testReplicatePackage() throws Exception {
        PackageVO packageVO = new PackageVO();
        packageVO.setHeight(10L);
        packageVO.setSignedTxList(Lists.newArrayList());
        logReplicateHandler.replicatePackage(packageVO);
    }

}