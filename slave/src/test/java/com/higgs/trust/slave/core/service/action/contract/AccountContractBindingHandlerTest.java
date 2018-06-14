package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountContractBindingHandlerTest extends IntegrateBaseTest {

    @Autowired SnapshotService snapshot;
    @Autowired private AccountContractBindingHandler actionHandler;
    @Autowired private AccountContractBindingSnapshotAgent agent;


    private AccountContractBindingAction createAction() {
        AccountContractBindingAction action = new AccountContractBindingAction();
        action.setAccountNo("zhangs");
        action.setContractAddress("895321051547e82e2018a204abe510e1b0e9a0843fd1ad4483a307d48bfe9754");
        action.setArgs("");
        action.setIndex(1);
        action.setType(ActionTypeEnum.BIND_CONTRACT);
        return action;
    }

    @org.junit.Test
    public void testValidate() {
        Action action = createAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER_POLICY)
                .addAction(action)
                .setTxId("00000000002")
                .signature("", "0x0000000000000000000000000")
                .makeBlockHeader()
                .build();


        snapshot.startTransaction();
        actionHandler.process(packContext);
        actionHandler.process(packContext);
        snapshot.commit();
    }

    @org.junit.Test
    public void testPersist() {
        Action action = createAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER_POLICY)
                .addAction(action)
                .setTxId("0000000000")
                .signature("", "0x0000000000000000000000000")
                .makeBlockHeader()
                .build();

        actionHandler.process(packContext);
    }
}