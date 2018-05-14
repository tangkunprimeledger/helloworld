package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.api.vo.QueryAccountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;

public class AccountInfoServiceImplTest extends BaseTest{

    @Autowired
    private AccountInfoService accountInfoService;

    @Test public void testQueryByAccountNos() throws Exception {

    }

    @Test public void testQueryAccountInfo() throws Exception {
        QueryAccountVO req = new QueryAccountVO();
        req.setAccountNo("");
        req.setDataOwner("Trust");
        List<AccountInfoVO> list = accountInfoService.queryAccountInfo(req);
        for (AccountInfoVO vo : list) {
            System.out.println(vo);
        }
    }
}