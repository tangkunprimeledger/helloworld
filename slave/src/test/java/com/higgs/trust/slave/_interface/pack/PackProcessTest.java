package com.higgs.trust.slave._interface.pack;

import com.alibaba.fastjson.JSONArray;
import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Slf4j
public class PackProcessTest extends InterfaceCommonTest{
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/pack/process/";

    @Autowired
    private PackageService packageService;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }

    @Test(dataProvider = "defaultProvider", priority = 1)
    public void testReceivedFail(Map<?, ?> param) {
        log.info("[testReceivedFail] param:{}", param);

        try {
            Package pack = getBodyData(param, Package.class);
            PackContext packContext = packageService.createPackContext(pack);
//            packageService.validating(packContext);
        } catch (SlaveException e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }
    }

    @Test(dataProvider = "defaultProvider", priority = 2)
    public void testReceivedSuccess(Map<?, ?> param) {
        log.info("[testReceivedSuccess] param:{}", param);
    }

    @Test(dataProvider = "defaultProvider", priority = 3)
    public void testValidatingFail(Map<?, ?> param) {
        log.info("[testValidatingFail] param:{}", param);

        try {
            Package pack = getBodyData(param, Package.class);
//            packageService.validateConsensus(pack);
        } catch (SlaveException e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }
    }

    @Test(dataProvider = "defaultProvider", priority = 4)
    public void testValidatingSuccess(Map<?, ?> param) {
        log.info("[testValidatingSuccess] param:{}", param);

        executeBeforeSql(param);

        try {
            Package pack = getBodyData(param, Package.class);
//            packageService.validateConsensus(pack);
        } catch (SlaveException e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }

        List<JSONArray> list = executeQuerySql(param);

        assertEquals(param.get("assert"), list.size());

        executeAfterSql(param);
    }


    @Test(dataProvider = "defaultProvider", priority = 5)
    public void testWaitValidateConsensusFail(Map<?, ?> param) {
        log.info("[testWaitValidateConsensusFail] param:{}", param);

        executeBeforeSql(param);

        try {
            Package pack = getBodyData(param, Package.class);
//            packageService.validated(pack);
        } catch (SlaveException e) {
            assertEquals(e.getMessage(), param.get("assert"));
        }

        executeAfterSql(param);

    }

    @Test(dataProvider = "defaultProvider", priority = 6)
    public void testWaitValidateConsensusSuccess(Map<?, ?> param) {
        log.info("[testWaitValidateConsensusSuccess] param:{}", param);
        
    }

    @Test(dataProvider = "defaultProvider", priority = 7)
    public void testValidatedFail(Map<?, ?> param) {
        log.info("[testValidatedFail] param:{}", param);

    }

    @Test(dataProvider = "defaultProvider", priority = 8)
    public void testValidatedSuccess(Map<?, ?> param) {
        log.info("[testValidatedSuccess] param:{}", param);

    }
}
