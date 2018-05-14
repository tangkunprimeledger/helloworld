package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
        coreTransactionService.submitTx(BizTypeEnum.STORAGE, coreTx);
    }

    @Test
    public void testSubmitTxSyncForPolicy(){
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId("tx_id_001");
        coreTx.setPolicyId("000000");
        coreTx.setBizModel(new JSONObject());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender("TRUST-TEST1");
        coreTx.setActionList(initPolicy());
        String signData = "my-sign";
        coreTransactionService.syncSubmitTxForEnd(BizTypeEnum.STORAGE, coreTx,signData);
        try {
            Thread.sleep(5000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Action> initPolicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-TEST1");
        registerPolicy.setRsIds(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);
        return registerPolicies;
    }


    @Test
    public void testProcessInitTx(){
        String txId = "";
        coreTransactionService.processInitTx(txId);
    }
}
