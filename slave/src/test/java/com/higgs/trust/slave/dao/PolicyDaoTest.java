package com.higgs.trust.slave.dao;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.dao.manage.PolicyDao;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import com.higgs.trust.slave.model.bo.manage.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/13 18:25
 * @desc policy dao test
 */
public class PolicyDaoTest extends IntegrateBaseTest {

    @Autowired PolicyDao policyDao;

    @Autowired PolicyRepository policyRepository;

    @Test public void queryByPolicyId() {
        Policy policy = policyRepository.getPolicyById("000000");
        Assert.assertEquals("[ALL]", JSON.toJSONString(policy.getRsIds()));
    }

    @Test public void testAdd1() {
        PolicyPO policy = new PolicyPO();
        policy.setPolicyId("000000");
        policy.setPolicyName("register");
        policy.setRsIds("[\"ALL\"]");

        policyDao.add(policy);
    }

    @Test public void testAdd2() {
        PolicyPO policy = new PolicyPO();
        policy.setPolicyId("policy-1hsdh6310-23hhs");
        policy.setPolicyName("register");
        List<String> rsIdList = new ArrayList<>();
        rsIdList.add("rs-test1");
        rsIdList.add("rs-test3");
        policy.setRsIds(JSON.toJSONString(rsIdList));

        policyDao.add(policy);
    }


}