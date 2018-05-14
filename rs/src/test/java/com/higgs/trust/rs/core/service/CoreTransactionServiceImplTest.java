package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.testframework.jsonutil.JsonFileUtil;
import com.higgs.trust.testframework.assertutil.AssertTool;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.Data;

public class CoreTransactionServiceImplTest extends IntegrateBaseTest {
    @Autowired private CoreTransactionServiceImpl coreTransactionService;
    //数据驱动
    @DataProvider
    public Object[][] provideValidatData(Method method){
        String path = JsonFileUtil.findJsonFile("java/com.higgs.trust.rs.core.service.coretransaction.submittx/exception");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][]) JsonFileUtil.jsonFileToArry(path);
        return arrmap;
    }
    @Test
    public void testSubmitTxValidate1(Map<?, ?> param) throws Exception {
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setPolicyId("1111");
        coreTransaction.setSender("sender");
        coreTransaction.setTxId("12234234234234");
        coreTransaction.setVersion("1.0");
        coreTransactionService.submitTx(BizTypeEnum.STORAGE,coreTransaction);
        String s = JSON.toJSONString(coreTransaction);
        System.out.println(s);
    }

    @Test(dataProvider = "provideValidatData")
    public void testSubmitTxValidate2(Map<?, ?> param) throws Exception {
        JSONObject obj = JSON.parseObject(param.get("coreTx").toString());

        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setVersion(obj.getString("version"));
        coreTransaction.setTxId(obj.getString("txId"));
        coreTransaction.setPolicyId(obj.getString("policyId"));
        coreTransaction.setSender(obj.getString("sender"));
        coreTransaction.setLockTime(obj.getDate("date"));
        coreTransaction.setBizModel(JSON.parseObject(obj.get("bizModel").toString()));
        System.out.println(param.get("type"));
        try {
            coreTransactionService.submitTx(BizTypeEnum.STORAGE,coreTransaction);
        }catch (Exception e){
            AssertTool.assertEquals(e.getMessage(),param.get("assert"));
        }

        String s = JSON.toJSONString(coreTransaction);
        System.out.println(s);
    }

}