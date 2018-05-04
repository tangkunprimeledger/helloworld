package com.higgs.trust.slave.core.service.action.account;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author hanson
 * @Date 2018/4/26
 * @Description:
 */
public class OpenAccountHandlerTest extends InterfaceCommonTest {

    private final static String rootPath = "java/com/higgs/trust/slave/core/service/accounting/openAccount/";

    @Autowired
    OpenAccountHandler openAccountHandler;

    @BeforeMethod()
    public void before() {
        super.before();

    }

    @AfterMethod
    public void after() {
        super.after();

    }

    @Override
    protected String getProviderRootPath() {
        return rootPath;
    }

    // TODO 搞明白开户的逻辑
    @Test(dataProvider = "defaultProvider", priority = 0)
    public void paramValidate(Map<?, ?> param) throws Exception {

        OpenAccount openAccount = getBodyData(param, OpenAccount.class);
        executeActionHandler(param,openAccountHandler,openAccount);

    }
    @Test(dataProvider = "defaultProvider", priority = 1)
    public void testException(Map<?, ?> param) throws Exception {
        OpenAccount openAccount = getBodyData(param, OpenAccount.class);
        executeActionHandler(param,openAccountHandler,openAccount);

    }


    @Test(dataProvider = "defaultProvider", priority = 2)
    public void testRegular(Map<?, ?> param) throws Exception {
        if (param.get("beforeSql") != null) {
            String[] sql = param.get("beforeSql").toString().split(";");
            DataBaseManager dataBaseManager = new DataBaseManager();
            for(String s:sql)  dataBaseManager.executeSingleDelete(s, DB_URL);
        }

        OpenAccount openAccount = getBodyData(param, OpenAccount.class);
        executeActionHandler(param,openAccountHandler,openAccount);

        if (param.get("afterSql") != null) {
            String[] sql = param.get("beforeSql").toString().split(";");
            DataBaseManager dataBaseManager = new DataBaseManager();
            for(String s:sql)
                dataBaseManager.executeSingleDelete(s, DB_URL);
        }

    }

}