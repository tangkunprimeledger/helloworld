package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.support.Assert;

public class ContractCreationHandlerTest extends IntegrateBaseTest {

    @Autowired SnapshotService snapshot;
    @Autowired private ContractCreationHandler creationHandler;
    @Autowired private ContractSnapshotAgent agent;


    private ContractCreationAction createContractCreationAction() {
        ContractCreationAction action = new ContractCreationAction();
        action.setCode("function main() { print('>>>>>>>> hello world 11<<<<<<<<<'); }");
        action.setLanguage("javascript");
        action.setVersion("1");
        action.setIndex(1);
        action.setType(ActionTypeEnum.REGISTER_CONTRACT);
        return action;
    }

    @Test
    public void testValidate() throws Exception {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId("000000000")
                .signature("kdkdkdk")
                .makeBlockHeader()
                .build();


        snapshot.startTransaction();
        creationHandler.validate(packContext);
        Contract contract = agent.get("a4155dddbbe5cb832404006eafd2fa3e46fef74f96b4c8a16239036d3b862aa7");
        snapshot.commit();
        Assert.isTrue(contract != null);
    }

    @Test
    public void testPersist() throws Exception {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId("0000000000")
                .signature("0x0000000000000000000000000")
                .makeBlockHeader()
                .build();

        creationHandler.persist(packContext);
    }
}