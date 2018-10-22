package com.higgs.trust.slave.core.service.consensus.log;

import com.google.common.collect.Lists;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class LogReplicateHandlerImplTest extends BaseTest {
    @Autowired LogReplicateHandler logReplicateHandler;

    @Test public void testReplicatePackage() throws Exception {
        List<Package> packageVOList = new LinkedList<>();
        Package pack = new Package();
        pack.setHeight(10L);
        pack.setSignedTxList(Lists.newArrayList());

        packageVOList.add(pack);
        logReplicateHandler.replicatePackage(new PackageCommand(null,pack));
    }

}