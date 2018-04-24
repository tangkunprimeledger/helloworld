package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.DataIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DataIdentitySnapshotAgentTest extends BaseTest{
    @Autowired
    private DataIdentitySnapshotAgent dataIdentitySnapshotAgent;
    @Autowired
    private SnapshotService snapshotService;
    @Test
    public void testGetDataIdentity() throws Exception {
        System.out.println("testGetDataIdentity:"+ dataIdentitySnapshotAgent.getDataIdentity("12312312"));
    }

    @Test
    public void testSaveDataIdentity() throws Exception {
        DataIdentity dataIdentity =  new DataIdentity();
        dataIdentity.setChainOwner("lingchao");
        dataIdentity.setDataOwner("lingchao");
        dataIdentity.setIdentity("lingchao");
        snapshotService.startTransaction();
        dataIdentitySnapshotAgent.saveDataIdentity(dataIdentity);
        snapshotService.commit();
        snapshotService.destroy();
        System.out.println("GetDataIdentity :" + dataIdentitySnapshotAgent.getDataIdentity(dataIdentity.getIdentity()));
        snapshotService.destroy();
    }

    @Test
    public void testQuery() throws Exception {
    }

}