package com.higgs.trust.slave._interface.accounting;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.AccountOperationHandler;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
@Slf4j public class OperationAccountTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/accounting/operationAccount/";

    @Autowired AccountOperationHandler accountOperationHandler;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param){
        log.info("[paramValidate]param:{}", param);
        AccountOperation action = getAction(param,AccountOperation.class,ActionTypeEnum.ACCOUNTING);
        executeActionHandler(param,accountOperationHandler,action);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param){
        log.info("[testRegular]param:{}", param);
        AccountOperation action = getAction(param,AccountOperation.class,ActionTypeEnum.ACCOUNTING);
        executeActionHandler(param,accountOperationHandler,action);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param){
        log.info("[testException]param:{}", param);
        executeBeforeSql(param);

        AccountOperation action = getAction(param,AccountOperation.class,ActionTypeEnum.ACCOUNTING);
        executeActionHandler(param,accountOperationHandler,action);

        executeAfterSql(param);
    }

}
