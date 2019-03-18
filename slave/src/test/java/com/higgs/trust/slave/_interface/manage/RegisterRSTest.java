package com.higgs.trust.slave._interface.manage;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.manage.RegisterRsHandler;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Map;

@Slf4j
public class RegisterRSTest extends InterfaceCommonTest{
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/manage/rs/";

    @Autowired
    private RegisterRsHandler registerRsHandler;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]param:{}", param);
        RegisterRS action = getAction(param, RegisterRS.class, ActionTypeEnum.REGISTER_RS);
        executeActionHandler(param, registerRsHandler, action);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        RegisterRS action = getAction(param, RegisterRS.class, ActionTypeEnum.REGISTER_RS);
        executeActionHandler(param, registerRsHandler, action);
        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param) {
        log.info("[testException]param:{}", param);
        executeBeforeSql(param);

        RegisterRS action = getAction(param, RegisterRS.class, ActionTypeEnum.REGISTER_RS);
        executeActionHandler(param, registerRsHandler, action);

        executeAfterSql(param);
    }
}
