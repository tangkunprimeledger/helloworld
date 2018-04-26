package com.higgs.trust.slave._interface.accounting;

import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.IssueCurrencyHandler;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author liuyu
 * @description
 * @date 2018-04-26
 */
@Slf4j public class IssueCurrencyTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/accounting/issueCurrency/";

    @Autowired IssueCurrencyHandler issueCurrencyHandler;

    @DataProvider public Object[][] defaultProvider(Method method) {
        String name = method.getName();
        String providerPath = PROVIDER_ROOT_PATH + name;
        log.info("[defaultProvider].path:{}", providerPath);
        String filePath = JsonFileUtil.findJsonFile(providerPath);
        HashMap<String, String>[][] arrMap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filePath);
        return arrMap;
    }

    @Test(dataProvider = "defaultProvider", priority = 0) public void paramValidate(Map<?, ?> param){
        log.info("[paramValidate]param:{}", param);
        IssueCurrency issueCurrency = getBodyData(param,IssueCurrency.class);
        if(issueCurrency!=null) {
            issueCurrency.setType(ActionTypeEnum.ISSUE_CURRENCY);
            issueCurrency.setIndex(1);
        }
        String assertData = getAssertData(param);
        try {
            issueCurrencyHandler.validate(makePackContext(issueCurrency, 1L));
            issueCurrencyHandler.persist(makePackContext(issueCurrency, 1L));
        }catch (Exception e){
            log.info("has error:{}",e.getMessage());
            assertEquals("x" +e.getMessage(),assertData);
        }
    }
}
