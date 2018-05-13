package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.util.Map;

public class ContractCreationHandlerInterfaceTest extends ContractBaseTest {

    @Autowired SnapshotService snapshot;
    @Autowired private ContractCreationHandler creationHandler;
    @Autowired private ContractSnapshotAgent agent;

    private ContractCreationAction createContractCreationAction(Map<?, ?> param) {
        ContractCreationAction action = getBody(param, ContractCreationAction.class);
        if (action != null) {
            action.setIndex(0);
            action.setType(ActionTypeEnum.REGISTER_CONTRACT);
        }
        return action;
    }

    private String getPolicyId(Map<?, ?> param) {
        JSONObject json = (JSONObject) param.get("transaction");
        return json == null ? null : json.getString("policyId");
    }

    private PackContext createPackContext(Map<?, ?> param) {
        Action action = createContractCreationAction(param);
        String policyId = getPolicyId(param);
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .setTransactionPolicyIdIf(policyId, !Strings.isNullOrEmpty(policyId))
                .addAction(action)
                .setTxId("00000000000" + System.currentTimeMillis())
                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();
        return packContext;
    }

    @Override
    public String getProviderRootPath() {
        return "java/com/higgs/trust/slave/core/service/contract/creation/";
    }

    @Test(dataProvider = "defaultProvider",priority = 0)
    public void testValidate(Map<?, ?> param) {
        PackContext packContext = createPackContext(param);
        snapshot.startTransaction();
        doTestValidate(param, packContext, creationHandler);
        snapshot.commit();
    }

    @Test(dataProvider = "defaultProvider",priority = 1)
    public void testPersist(Map<?, ?> param) {
        PackContext packContext = createPackContext(param);
        doTestPersist(param, packContext, creationHandler);
    }

    @AfterClass
    public void clearDb() {
        //executeDelete("TRUNCATE TABLE contract;");
    }
}