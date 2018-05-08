package com.higgs.trust.slave._interface.manage;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.manage.RegisterPolicyHandler;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Map;

@Slf4j public class PolicyTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/manage/policy/";

    @Autowired private RegisterPolicyHandler registerPolicyHandler;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    private void clearDB() {
        StringBuilder sql = new StringBuilder();
        sql.append("truncate table policy;");
        DataBaseManager dataBaseManager = new DataBaseManager();
        dataBaseManager.executeSingleDelete(sql.toString(), DB_URL);
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]param:{}", param);
        RegisterPolicy action = getAction(param, RegisterPolicy.class, ActionTypeEnum.REGISTER_POLICY);
        executeActionHandler(param, registerPolicyHandler, action);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        RegisterPolicy action = getAction(param, RegisterPolicy.class, ActionTypeEnum.REGISTER_POLICY);
        executeActionHandler(param, registerPolicyHandler, action);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param) {
        log.info("[testException]param:{}", param);
        RegisterPolicy action = getAction(param, RegisterPolicy.class, ActionTypeEnum.REGISTER_POLICY);
        executeActionHandler(param, registerPolicyHandler, action);
    }
}
