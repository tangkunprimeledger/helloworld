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
        AccountFreeze freeze = getBodyData(param, AccountFreeze.class);
        executeActionHandler(param, accountFreezeHandler, freeze);

    }

    @Test(dataProvider = "defaultProvider", priority = 2)
    public void testRegular(Map<?, ?> param) throws Exception {
        JSONObject object = (JSONObject)param.get("body");

        AccountFreeze freeze = getObject(object.get("freeze").toString(), AccountFreeze.class);
        OpenAccount creditAccount = getObject(object.get("credit").toString(), OpenAccount.class);
        OpenAccount debitAccount = getObject(object.get("debit").toString(), OpenAccount.class);
        AccountOperation accountOperation = getObject(object.get("accounting").toString(), AccountOperation.class);
        String policyId = object.getString("policyId");

        if (param.get("beforeSql") != null) {
            String[] sql = ((JSONArray)param.get("beforeSql")).toArray(new String[] {});
            DataBaseManager dataBaseManager = new DataBaseManager();
            for (String s : sql)
                dataBaseManager.executeSingleDelete(s, DB_URL);
        }
        // AccountFreeze freeze = getBodyData(param, AccountFreeze.class);
        openAccountHandler.validate(makePackContext(creditAccount, 1L, null));
        openAccountHandler.persist(makePackContext(creditAccount, 1L, null));
        openAccountHandler.validate(makePackContext(debitAccount, 1L,null));
        openAccountHandler.persist(makePackContext(debitAccount, 1L,null));

        PackContext packContext = makePackContext(accountOperation, 1L,null);
        packContext.getCurrentTransaction().getCoreTx().setPolicyId(policyId);

//        RegisterPolicy policy = new RegisterPolicy();
//        policy.setPolicyName("test");
//        policy.setPolicyId(policyId);
//        List<String> list = new ArrayList<String>();
//        list.add("b");
//        policy.setRsIds(list);
//        policy.setIndex(1);
//        policy.setType(REGISTER_POLICY);
//        manageSnapshotAgent.registerPolicy(policy);
//        policyRepository.save(policyRepository.convertActionToPolicy(policy));


        accountOperationHandler.validate(packContext);
//        packContext.getCurrentTransaction().getCoreTx().setPolicyId("000001");
        accountOperationHandler.persist(packContext);

        accountFreezeHandler.validate(makePackContext(freeze, 1L,null));
        accountFreezeHandler.persist(makePackContext(freeze, 1L,null));

        if (param.get("afterSql") != null) {
            String[] sql = ((JSONArray)param.get("beforeSql")).toArray(new String[] {});
            DataBaseManager dataBaseManager = new DataBaseManager();
            for (String s : sql)
                dataBaseManager.executeSingleDelete(s, DB_URL);
        }
    }

}