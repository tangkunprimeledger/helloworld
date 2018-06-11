package com.higgs.trust.slave.core.service.action.dataidentity;

import com.alibaba.fastjson.JSONArray;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.DataIdentitySnapshotAgent;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.List;

public class DataIdentityActionHandlerTest  extends BaseTest {
    @Autowired
    private DataIdentityActionHandler dataIdentityActionHandler;
    @Autowired
    private SnapshotService snapshotService;
    @Autowired
    private DataIdentitySnapshotAgent dataIdentitySnapshotAgent;
    @Test
    public void testValidate() throws Exception {
       snapshotService.clear();
        snapshotService.startTransaction();
        List<DataIdentity> list =  new ArrayList<>();
        DataIdentity dataIdentity1 = new DataIdentity();
        dataIdentity1.setIdentity("12321");
        dataIdentity1.setDataOwner("trust");
        dataIdentity1.setChainOwner("trust");
        list.add(dataIdentity1);
        DataIdentity dataIdentity2 = new DataIdentity();
        dataIdentity2.setIdentity("12321");
        dataIdentity2.setDataOwner("trust");
        dataIdentity2.setChainOwner("trust");
        list.add(dataIdentity2);
        System.out.println(JSONArray.toJSONString(list));

        List<String> rsList =  new ArrayList<>();
        rsList.add("RS");
        rsList.add("RS");
        System.out.println(JSONArray.toJSONString(rsList));

        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setChainOwner("lll");
        dataIdentityAction.setDataOwner("3wew");
        dataIdentityAction.setIdentity("12312312321");
        dataIdentityAction.setIndex(1);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        System.out.println(JSONArray.toJSONString(dataIdentityAction));
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
        dataIdentityActionHandler.process(ActionData);
       snapshotService.commit();
        snapshotService.flush();
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
    }

}