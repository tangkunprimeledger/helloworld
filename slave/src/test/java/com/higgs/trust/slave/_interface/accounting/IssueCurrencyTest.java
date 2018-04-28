package com.higgs.trust.slave._interface.accounting;

import com.higgs.trust.slave.JsonFileUtil;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.IssueCurrencyHandler;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
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
    private static String DB_URL = "jdbc:mysql://localhost:3306/trust?user=root&password=root";

    @Autowired IssueCurrencyHandler issueCurrencyHandler;

    @AfterMethod
    public void after(Method method){
        super.after();
        String name = method.getName();
        if(!StringUtils.equals("testRegular",name)){
            clearDB();
        }
    }

    private void clearDB(){
        String sql = "truncate table currency_info;";
        DataBaseManager dataBaseManager = new DataBaseManager();
        dataBaseManager.executeSingleDelete(sql,DB_URL);
    }

    @DataProvider public Object[][] defaultProvider(Method method) {
        String name = method.getName();
        String providerPath = PROVIDER_ROOT_PATH + name;
        log.info("[defaultProvider].path:{}", providerPath);
        String filePath = JsonFileUtil.findJsonFile(providerPath);
        HashMap<String, String>[][] arrMap = (HashMap<String, String>[][])JsonFileUtil.jsonFileToArry(filePath);
        return arrMap;
    }

    private void execute(Map<?, ?> param,IssueCurrency issueCurrency){
        String assertData = getAssertData(param);
        try {
            issueCurrencyHandler.validate(makePackContext(issueCurrency, 1L));
            issueCurrencyHandler.persist(makePackContext(issueCurrency, 1L));
        }catch (Exception e){
            log.info("has error:{}",e.getMessage());
            assertEquals(e.getMessage(),assertData);
        }
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void paramValidate(Map<?, ?> param){
        log.info("[paramValidate]param:{}", param);
        IssueCurrency issueCurrency = getBodyData(param,IssueCurrency.class);
        if(issueCurrency!=null) {
            issueCurrency.setType(ActionTypeEnum.ISSUE_CURRENCY);
            issueCurrency.setIndex(1);
        }
        execute(param,issueCurrency);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void testRegular(Map<?, ?> param){
        log.info("[testRegular]param:{}", param);
        IssueCurrency issueCurrency = getBodyData(param,IssueCurrency.class);
        if(issueCurrency!=null) {
            issueCurrency.setType(ActionTypeEnum.ISSUE_CURRENCY);
            issueCurrency.setIndex(1);
        }
        execute(param,issueCurrency);
    }

    @Test(dataProvider = "defaultProvider", priority = 3) public void testException(Map<?, ?> param){
        log.info("[testException]param:{}", param);
        IssueCurrency issueCurrency = getBodyData(param,IssueCurrency.class);
        if(issueCurrency!=null) {
            issueCurrency.setType(ActionTypeEnum.ISSUE_CURRENCY);
            issueCurrency.setIndex(1);
        }
        execute(param,issueCurrency);
    }

}
