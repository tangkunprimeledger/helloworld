package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.*;

public class DataIdentityRepositoryTest extends BaseTest{
    @Autowired
    private DataIdentityRepository dataIdentityRepository;
    @Test
    public void testQueryDataIdentity() throws Exception {
        System.out.println("queryByIdentity:" + dataIdentityRepository.queryDataIdentity("12312312"));
    }

    @Test
    public void testSave() throws Exception {
        DataIdentity dataIdentityPO = new DataIdentity();
        dataIdentityPO.setIdentity("123wew123"+new Date());
        dataIdentityPO.setDataOwner("wangxinlicai-rsid");
        dataIdentityPO.setChainOwner("bitUn");
        dataIdentityRepository.save(dataIdentityPO);
    }

}