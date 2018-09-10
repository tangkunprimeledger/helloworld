package com.higgs.trust.rs.core.repository;

import com.higgs.trust.IntegrateBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BizTypeRepositoryTest extends IntegrateBaseTest{

    @Autowired
    private BizTypeRepository bizTypeRepository;
    @Test public void testAdd() {
        Assert.assertEquals(
            bizTypeRepository.add("test-policy", "register cass"), "add biz type success");
    }

    @Test public void testGetByPolicyId() throws Exception {
        bizTypeRepository.getByPolicyId("test-policy");
    }
}