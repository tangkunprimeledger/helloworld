package com.higgs.trust.slave.core.service.action.dataidentity;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.DataIdentitySnapshotAgent;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class DataIdentityActionHandlerTest extends BaseTest {
    @Autowired
    private DataIdentityActionHandler dataIdentityActionHandler;
    @Autowired
    private SnapshotService snapshotService;
    @Autowired
    private DataIdentitySnapshotAgent dataIdentitySnapshotAgent;
    @Test
    public void testValidate() throws Exception {
        snapshotService.destroy();
        snapshotService.startTransaction();
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setChainOwner("lll");
        dataIdentityAction.setDataOwner("3wew");
        dataIdentityAction.setIdentity("12312312321");
        dataIdentityAction.setIndex(1);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        ActionData ActionData = new ActionData() {
            @Override
            public Block getCurrentBlock() {
                return null;
            }

            @Override
            public Package getCurrentPackage() {
                return null;
            }

            @Override
            public SignedTransaction getCurrentTransaction() {
                return null;
            }

            @Override
            public Action getCurrentAction() {
                return dataIdentityAction;
            }
        };
        dataIdentityActionHandler.validate(ActionData);
        snapshotService.commit();
        System.out.println("9999999999"+dataIdentitySnapshotAgent.getDataIdentity("12312312321"));

    }

    @Test
    public void testPersist() throws Exception {
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setChainOwner("lll");
        dataIdentityAction.setDataOwner("3wew");
        dataIdentityAction.setIdentity("12312312321");
        dataIdentityAction.setIndex(1);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        ActionData ActionData = new ActionData() {
            @Override
            public Block getCurrentBlock() {
                return null;
            }

            @Override
            public Package getCurrentPackage() {
                return null;
            }

            @Override
            public SignedTransaction getCurrentTransaction() {
                return null;
            }

            @Override
            public Action getCurrentAction() {
                return dataIdentityAction;
            }
        };
        dataIdentityActionHandler.persist(ActionData);
    }

}