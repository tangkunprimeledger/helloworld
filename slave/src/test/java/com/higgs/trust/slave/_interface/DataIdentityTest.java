package com.higgs.trust.slave._interface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityActionHandler;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityService;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * dataIdentity test
 *
 * @author lingchao
 * @create 2018年05月08日18:56
 */
@Slf4j
public class DataIdentityTest extends InterfaceCommonTest {
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/action/dataIdentity/";
    @Autowired
    private DataIdentityActionHandler dataIdentityActionHandler;

    @Autowired
    private DataIdentityService dataIdentityService;

    @Override
    protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1)
    public void paramValidate(Map<?, ?> param) {
        log.info("[paramValidate]{}", param.get("comment"));
        DataIdentityAction action = getAction(param, DataIdentityAction.class, ActionTypeEnum.CREATE_DATA_IDENTITY);
        executeActionHandler(param, dataIdentityActionHandler, action);
    }

    @Test(dataProvider = "defaultProvider", priority = 2)
    public void testRegular(Map<?, ?> param) {
        log.info("[testRegular]{}", param.get("comment"));


        executeBeforeSql(param);

        DataIdentityAction action = getAction(param, DataIdentityAction.class, ActionTypeEnum.CREATE_DATA_IDENTITY);
        executeActionHandler(param, dataIdentityActionHandler, action);

        checkResults(param);

        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 3)
    public void testException(Map<?, ?> param) {
        log.info("[testException]{}", param.get("comment"));
        executeBeforeSql(param);

        DataIdentityAction action = getAction(param, DataIdentityAction.class, ActionTypeEnum.CREATE_DATA_IDENTITY);
        executeActionHandler(param, dataIdentityActionHandler, action);

        executeAfterSql(param);
    }


    @Test(dataProvider = "defaultProvider", priority = 4)
    public void serviceParamValidate(Map<?, ?> param) {
        log.info("[paramValidate]{}", param.get("comment"));
        String assertData = getAssertData(param);
        List<String> rsList = getRsList(param);
        List<DataIdentity> dataIdentityList = getDataIdentityList(param);
        boolean isTrue = dataIdentityService.validate(rsList, dataIdentityList);
        assertEquals(isTrue, Boolean.parseBoolean(assertData));
    }


    @Test(dataProvider = "defaultProvider", priority = 5)
    public void serviceTestRegular(Map<?, ?> param) {
        log.info("[serviceTestRegular]{}", param.get("comment"));
        String assertData = getAssertData(param);
        List<String> rsList = getRsList(param);
        List<DataIdentity> dataIdentityList = getDataIdentityList(param);
        boolean isTrue = dataIdentityService.validate(rsList, dataIdentityList);
        assertEquals(isTrue, Boolean.parseBoolean(assertData));
    }


    @Test(dataProvider = "defaultProvider", priority = 6)
    public void serviceTestException(Map<?, ?> param) {
        log.info("[serviceTestException]{}", param.get("comment"));
        String assertData = getAssertData(param);
        List<String> rsList = getRsList(param);
        List<DataIdentity> dataIdentityList = getDataIdentityList(param);
        boolean isTrue = dataIdentityService.validate(rsList, dataIdentityList);
        assertEquals(isTrue, Boolean.parseBoolean(assertData));
    }


    private List<String> getRsList(Map<?, ?> param) {
        String rsListStr = String.valueOf(param.get("rsList"));

        List<String> rsList = null;
        if (StringUtils.isEmpty(rsListStr)) {
            return rsList;
        }
        rsListStr = rsListStr.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",", "");
        return JSON.parseObject(rsListStr, new TypeReference<List<String>>() {
        });
    }

    private List<DataIdentity> getDataIdentityList(Map<?, ?> param) {
        String dataIdentityListStr = String.valueOf(param.get("dataIdentityList"));
        List<DataIdentity> dataIdentityList = null;
        if (StringUtils.isEmpty(dataIdentityListStr)) {
            return dataIdentityList;
        }
        dataIdentityListStr = dataIdentityListStr.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",", "");
        return JSON.parseObject(dataIdentityListStr, new TypeReference<List<DataIdentity>>() {
        });
    }
}
