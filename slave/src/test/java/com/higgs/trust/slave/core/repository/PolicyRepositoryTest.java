package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PolicyRepositoryTest extends BaseTest {

    @Autowired
    private PolicyRepository policyRepository;

    private Policy policy;

    @BeforeMethod public void setUp() throws Exception {
        List<String> rsIds = new ArrayList<>();
        rsIds.add("rs-test1");
        rsIds.add("rs-test3");
        rsIds.add("rs-test3");
        policy = new Policy();
        policy.setPolicyId("policy-test-1");
        policy.setPolicyName("注册policy-test-1");
        policy.setRsIds(rsIds);
    }

    @Test public void getPolicyById() {
        Policy policy = policyRepository.getPolicyById("test");
        Assert.assertEquals(null, policy);
    }

    @Test public void save() {
        policyRepository.save(policy);
    }

    @Test public void convertActionToPolicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-0000");
        registerPolicy.setPolicyName("测试test");
        List<String> rsIds = new ArrayList<>();
        rsIds.add("rs-test1");
        rsIds.add("rs-test2");
        rsIds.add("rs-test2");
        registerPolicy.setRsIds(rsIds);

        Policy policy = policyRepository.convertActionToPolicy(registerPolicy);

        Assert.assertEquals(policy.getPolicyId(), registerPolicy.getPolicyId());
        Assert.assertEquals(policy.getPolicyName(), registerPolicy.getPolicyName());
        Assert.assertEquals(policy.getRsIds(), registerPolicy.getRsIds());
    }

    @Test
    public void getPolicyType() {
        String type = policyRepository.getPolicyType("000000");
        Assert.assertEquals("REGISTER", type);
    }
}