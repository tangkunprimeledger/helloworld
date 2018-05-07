package com.higgs.trust.slave._interface.accounting;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.IssueCurrencyHandler;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
@Slf4j public class IssueCurrencyTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/accounting/issueCurrency/";

    @Autowired IssueCurrencyHandler issueCurrencyHandler;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]{}", param.get("comment"));
        IssueCurrency issueCurrency = getAction(param, IssueCurrency.class, ActionTypeEnum.ISSUE_CURRENCY);
        executeActionHandler(param, issueCurrencyHandler, issueCurrency);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]{}", param.get("comment"));
        IssueCurrency issueCurrency = getAction(param, IssueCurrency.class, ActionTypeEnum.ISSUE_CURRENCY);
        executeActionHandler(param, issueCurrencyHandler, issueCurrency);

        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param) {
        log.info("[testException]{}", param.get("comment"));
        executeBeforeSql(param);

        IssueCurrency issueCurrency = getAction(param, IssueCurrency.class, ActionTypeEnum.ISSUE_CURRENCY);
        executeActionHandler(param, issueCurrencyHandler, issueCurrency);

        executeAfterSql(param);
    }

}
