package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
public class CoreTxServiceTest extends IntegrateBaseTest{
    @Autowired private CoreTransactionService coreTransactionService;

    @Test
    public void testSubmitTx(){
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId("tx_id_001");
        coreTx.setPolicyId("test-policy-1");
        coreTx.setBizModel(new JSONObject());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender("RS001");
        String signData = "my-sign";
        coreTransactionService.submitTx(BizTypeEnum.STORAGE, coreTx,signData);
    }
    @Test
    public void testProcessInitTx(){
        String txId = "";
        coreTransactionService.processInitTx(txId);
    }
}
