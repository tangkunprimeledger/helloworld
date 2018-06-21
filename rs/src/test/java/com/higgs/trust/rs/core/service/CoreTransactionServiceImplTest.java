package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.testframework.jsonutil.JsonFileUtil;
import com.higgs.trust.testframework.assertutil.AssertTool;
import com.higgs.trust.testframework.dbunit.DataBaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public void testSubmitTxValidate1() throws Exception {
        DataBaseManager dataBaseManager = new DataBaseManager();
        dataBaseManager.executeSingleDelete("delete from core_transaction where tx_id = '12234234234234';",DB_URL);
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setPolicyId(InitPolicyEnum.REGISTER_POLICY.getPolicyId());
        coreTransaction.setSender("sender");
        coreTransaction.setTxId("12234234234234");
        coreTransaction.setVersion("1.0");
        coreTransaction.setBizModel(new JSONObject());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = "2012-3-1";
        Date date=sdf.parse(strDate);
        coreTransaction.setLockTime(date);
        coreTransactionService.submitTx(coreTransaction);
        String s = JSON.toJSONString(coreTransaction);
        System.out.println(s);
        JSONObject jsonObject = JSON.parseObject("{\"tx_id\":\"12234234234234\"}");
       // AssertTool.isContainsExpectJsonNode("{\"tx_id\":\"12234234234234\"}",DB_URL,"SELECT * from core_transaction WHERE sender = 'sender'");
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
            coreTransactionService.submitTx(coreTransaction);
        }catch (Exception e){
            AssertTool.assertEquals(e.getMessage(),param.get("assert"));
        }

        String s = JSON.toJSONString(coreTransaction);
        System.out.println(s);
    }

    @Test(expectedExceptions = RsCoreException.class)
    public void testSubmitTxValidate3() throws Exception {
        CoreTransaction coreTransaction = null;
        coreTransactionService.submitTx(coreTransaction);
    }

    @Test
    public void testSubmitTxValidate4() throws Exception{
        DataBaseManager dataBaseManager = new DataBaseManager();
        dataBaseManager.executeSingleDelete("delete from core_transaction where tx_id = '122342342342345';",DB_URL);
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setPolicyId("11115");
        coreTransaction.setSender("sender");
        coreTransaction.setTxId("122342342342345");
        coreTransaction.setVersion("1.0");
        try {
            coreTransactionService.submitTx(coreTransaction);
        }catch (Exception e){
            AssertTool.assertEquals(e.getMessage(),"tx bizType is null[RS_CORE_TX_BIZ_TYPE_IS_NULL]");
        }

    }
}