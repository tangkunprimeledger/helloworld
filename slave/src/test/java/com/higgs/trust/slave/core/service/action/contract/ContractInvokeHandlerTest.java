package com.higgs.trust.slave.core.service.action.contract;


import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.Contract;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.support.Assert;

public class ContractInvokeHandlerTest extends IntegrateBaseTest {

    @Autowired private ContractInvokeHandler invokeHandler;

    private ContractInvokeAction createContractInvokeAction() {
        ContractInvokeAction action = new ContractInvokeAction();
        action.setAddress("895321051547e82e2018a204abe510e1b0e9a0843fd1ad4483a307d48bfe9754");
        //action.setMethod("main");
        action.setIndex(1);
        action.setType(ActionTypeEnum.REGISTER_CONTRACT);
        return action;
    }

    @Test
    public void testValidate() throws Exception {
        Action action = createContractInvokeAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId("000000000")
                .signature("kdkdkdk")
                .makeBlockHeader()
                .build();
        invokeHandler.validate(packContext);
    }

    @Test
    public void testPersist() throws Exception {
        Action action = createContractInvokeAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId("000000000")
                .signature("kdkdkdk")
                .makeBlockHeader()
                .build();
        invokeHandler.persist(packContext);
    }

}