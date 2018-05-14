package com.higgs.trust.slave._interface.accounting;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.AccountUnFreezeHandler;
import com.higgs.trust.slave.model.bo.account.AccountUnFreeze;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
@Slf4j public class UnFreezeTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/accounting/unFreezeAccount/";

    @Autowired AccountUnFreezeHandler accountUnFreezeHandler;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]{}", param.get("comment"));
        AccountUnFreeze action = getAction(param, AccountUnFreeze.class, ActionTypeEnum.ACCOUNTING);
        executeActionHandler(param, accountUnFreezeHandler, action);
    }

//    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param) {
//        log.info("[testRegular]{}", param.get("comment"));
//        executeBeforeSql(param);
//
//        AccountUnFreeze action = getAction(param, AccountUnFreeze.class, ActionTypeEnum.ACCOUNTING);
//        executeActionHandler(param, accountUnFreezeHandler, action);
//
//        checkResults(param);
//
//        executeAfterSql(param);
//    }

//    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param) {
//        log.info("[testException]{}", param.get("comment"));
//        executeBeforeSql(param);
//
//        AccountUnFreeze action = getAction(param, AccountUnFreeze.class, ActionTypeEnum.ACCOUNTING);
//        executeActionHandler(param, accountUnFreezeHandler, action);
//
//        executeAfterSql(param);
//    }

}
