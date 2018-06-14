package com.higgs.trust.slave.core.service.transaction;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.bo.context.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TransactionExecutorImplTest extends BaseTest {
    @Autowired private TransactionExecutor transactionExecutor;
    @Test
    public void testValidate() throws Exception {
        PackageData packageData = new PackageData() {
            @Override
            public void setCurrentTransaction(SignedTransaction transaction) {

            }

            @Override
            public TransactionData parseTransactionData() {
                return null;
            }

            @Override
            public void setCurrentPackage(Package currentPackage) {

            }

            @Override
            public void setCurrentBlock(Block block) {

            }

            @Override public void setRsPubKeyMap(Map<String, String> rsPubKeyMap) {

            }

            @Override public Map<String, String> getRsPubKeyMap() {
                return null;
            }

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
                return null;
            }
        };

        SignedTransaction signedTransaction = new SignedTransaction();
        CoreTransaction coreTransaction = new CoreTransaction();
        List<Action> actionList = new ArrayList<>();
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        dataIdentityAction.setIndex(0);
        dataIdentityAction.setIdentity("123");
        dataIdentityAction.setDataOwner("1234");
        dataIdentityAction.setChainOwner("12345");

        DataIdentityAction dataIdentityAction1 = new DataIdentityAction();
        dataIdentityAction1.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        dataIdentityAction1.setIndex(1);
        dataIdentityAction1.setIdentity("123");
        dataIdentityAction1.setDataOwner("1234");
        dataIdentityAction1.setChainOwner("12345");
        actionList.add(dataIdentityAction);
        actionList.add(dataIdentityAction1);

        coreTransaction.setActionList(actionList);
        coreTransaction.setPolicyId("12321");
        coreTransaction.setSender("wer");
        coreTransaction.setTxId("123123123");
        coreTransaction.setVersion("12312");
        coreTransaction.setLockTime(new Date());
        coreTransaction.setBizModel(new JSONObject());

        signedTransaction.setCoreTx(coreTransaction);
        packageData.setCurrentTransaction(signedTransaction);


//        transactionExecutor.validate(packageData.parseTransactionData());
    }

}