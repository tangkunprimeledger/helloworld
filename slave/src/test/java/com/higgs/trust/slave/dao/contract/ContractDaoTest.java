package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import reactor.core.support.Assert;

public class ContractDaoTest extends BaseTest {

    @Autowired ContractDao contractDao;

    @Test
    public void testAdd() {
        ContractPO contract = new ContractPO();
        contract.setAddress("bf9818a20dff5e1eebeeb31f58279401751b5abdf102e371220703f78198d83d");
        contract.setLanguage("javascript");
        contract.setCode("print('good')");
        contractDao.add(contract);
    }

    @Test
    public void testQueryByAddress() throws Exception {
        ContractPO contract = contractDao.queryByAddress("bf9818a20dff5e1eebeeb31f58279401751b5abdf102e371220703f78198d83d");
        Assert.isTrue(contract.getAddress().equals("bf9818a20dff5e1eebeeb31f58279401751b5abdf102e371220703f78198d83d"));
    }
}
