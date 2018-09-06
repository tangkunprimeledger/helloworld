package com.higgs.trust.slave.core.service.consensus.log;

import com.google.common.collect.Lists;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class LogReplicateHandlerImplTest extends BaseTest {
    @Autowired LogReplicateHandler logReplicateHandler;

    @Test public void testReplicatePackage() throws Exception {
        List<PackageVO> packageVOList = new LinkedList<>();
        PackageVO packageVO = new PackageVO();
        packageVO.setHeight(10L);
        packageVO.setSignedTxList(Lists.newArrayList());

        packageVOList.add(packageVO);
        logReplicateHandler.replicatePackage(new PackageCommand(1L,1L,null,packageVO));
    }

}