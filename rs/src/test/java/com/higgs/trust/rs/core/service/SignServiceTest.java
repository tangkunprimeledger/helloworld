package com.higgs.trust.rs.core.service;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author liuyu
 * @description
 * @date 2018-05-15
 */
public class SignServiceTest extends IntegrateBaseTest{
    @Autowired SignService signService;
    @Test
    public void testSign(){
        CoreTransaction coreTx = new CoreTransaction();
        SignInfo signInfo = signService.signTx(coreTx);
        System.out.println("---->:" + signInfo);
    }
}
