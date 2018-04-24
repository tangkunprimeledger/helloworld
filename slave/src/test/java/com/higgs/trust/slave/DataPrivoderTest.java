package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;

public class DataPrivoderTest {

    @Autowired
    private com.higgs.trust.slave.core.service.action.utxo.UTXOActionHandler UTXOActionHandler;
    //数据驱动
    @DataProvider
    public Object[][] provideNumbers(Method method){
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][]) JsonFileUtil.jsonFileToArry("./src/test/resources/java/com/higgs/trust/slave/core/service");
        //HashMap<String, String>[][] arrmap = (HashMap<String, String>[][]) JsonFileUtil.listToArry("./test-classes/java/com/higgs/trust/slave/biz/core/");
        return arrmap;
    }

    @Test(dataProvider = "provideNumbers")
    public void testValidate(Map<?, ?> param) throws InterruptedException{
        System.out.println(param.get("body"));
        System.out.println(param.get("comment"));
        JSONObject obj = JSON.parseObject(param.get("body").toString());
        System.out.println(obj);
        System.out.println(JSON.parseObject(obj.get("utxoAction").toString()));
        System.out.println(JSON.parseObject(obj.get("coreTxSnapshot").toString()));
        UTXOAction action = JSON.parseObject(obj.get("utxoAction").toString(),UTXOAction.class);
    }
}
