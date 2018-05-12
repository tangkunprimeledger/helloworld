package com.higgs.trust.slave._interface.pack;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Slf4j
public class PackCreateTest extends InterfaceCommonTest{
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/pack/create/";

    @Autowired
    private PackageService packageService;
    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1) public void fail(Map<?, ?> param) {
        log.info("[paramValidate]param:{}", param);
        executeBeforeSql(param);

        Package pack = packageService.create(getTxList(param));
        assertEquals(pack, param.get("assert"));

        executeAfterSql(param);
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void success(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        Package pack = packageService.create(getTxList(param));
        assertEquals(String.valueOf(pack.getHeight()), getAssertData(param));
    }

    @Test(dataProvider = "defaultProvider", priority = 2) public void successWithCondition(Map<?, ?> param) {
        log.info("[testRegular]param:{}", param);
        executeBeforeSql(param);

        Package pack = packageService.create(getTxList(param));

        assertEquals(String.valueOf(pack.getHeight()), getAssertData(param));

        executeAfterSql(param);
    }

    private List<SignedTransaction> getTxList(Map<?, ?> param) {
        String body = String.valueOf(param.get("body"));
        if (StringUtils.isEmpty(body) || "null".equals(body)) {
            return null;
        }
        body = body.replaceAll("\"@type\":\"com.alibaba.fastjson.JSONObject\",", "");
        return JSON.parseArray(body, SignedTransaction.class);
    }
}
