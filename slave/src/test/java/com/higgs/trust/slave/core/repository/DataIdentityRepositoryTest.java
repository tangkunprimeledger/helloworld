package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class DataIdentityRepositoryTest extends BaseTest{
    @Autowired
    private DataIdentityRepository dataIdentityRepository;
    @Test
    public void testQueryDataIdentity() throws Exception {
        System.out.println("queryByIdentity:" + dataIdentityRepository.queryDataIdentity("12312312"));
    }

}