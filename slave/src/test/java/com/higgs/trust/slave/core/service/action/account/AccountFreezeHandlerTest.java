package com.higgs.trust.slave.core.service.action.account;

import static com.higgs.trust.slave.api.enums.ActionTypeEnum.REGISTER_POLICY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.snapshot.agent.ManageSnapshotAgent;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.tester.dbunit.DataBaseManager;

/**
 * @author hanson
 * @Date 2018/4/28
 * @Description:
 */
public class AccountFreezeHandlerTest extends InterfaceCommonTest {

    private final static String rootPath = "java/com/higgs/trust/slave/core/service/accounting/freezeAccount/";

    @Autowired
    AccountFreezeHandler accountFreezeHandler;

    @Autowired
    OpenAccountHandler openAccountHandler;

    @Autowired
    AccountOperationHandler accountOperationHandler;
    @Autowired
    ManageSnapshotAgent manageSnapshotAgent;

    @Autowired
    PolicyRepository policyRepository;

    @Override
    protected String getProviderRootPath() {
        return rootPath;
    }

    @Test(dataProvider = "defaultProvider", priority = 0)
    public void paramValidate(Map<?, ?> param) throws Exception {
        AccountFreeze freeze = getBodyData(param, AccountFreeze.class);
        executeActionHandler(param, accountFreezeHandler, freeze);
    }

    @Test(dataProvider = "defaultProvider", priority = 1)
    public void testException(Map<?, ?> param) throws Exception {
        executeBeforeSql(param);
        AccountFreeze freeze = getBodyData(param, AccountFreeze.class);
        executeActionHandler(param, accountFreezeHandler, freeze);
        executeAfterSql(param);

    }

    @Test(dataProvider = "defaultProvider", priority = 2)
    public void testRegular(Map<?, ?> param) throws Exception {
        JSONObject object = (JSONObject)param.get("body");

        AccountFreeze freeze = getObject(object.get("freeze").toString(), AccountFreeze.class);
//        OpenAccount creditAccount = getObject(object.get("credit").toString(), OpenAccount.class);
//        OpenAccount debitAccount = getObject(object.get("debit").toString(), OpenAccount.class);
//        AccountOperation accountOperation = getObject(object.get("accounting").toString(), AccountOperation.class);
//        String policyId = object.getString("policyId");

        executeBeforeSql(param);

//        openAccountHandler.validate(makePackContext(creditAccount, 1L));
//        openAccountHandler.persist(makePackContext(creditAccount, 1L));
//        openAccountHandler.validate(makePackContext(debitAccount, 1L));
//        openAccountHandler.persist(makePackContext(debitAccount, 1L));
//
//        PackContext packContext = makePackContext(accountOperation, 1L);
//        packContext.getCurrentTransaction().getCoreTx().setPolicyId(policyId);
//
//        accountOperationHandler.validate(packContext);
//        accountOperationHandler.persist(packContext);
        PackContext packContext = makePackContext(freeze, 1L,param);

        accountFreezeHandler.validate(packContext);
        accountFreezeHandler.persist(packContext);

//        accountFreezeHandler.validate(makePackContext(freeze, 1L));
//        accountFreezeHandler.persist(makePackContext(freeze, 1L));
        executeAfterSql(param);

    }

}