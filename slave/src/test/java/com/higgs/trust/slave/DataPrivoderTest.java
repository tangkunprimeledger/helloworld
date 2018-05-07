package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.tester.assertutil.AssertTool;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DataPrivoderTest{

    @Autowired
    private com.higgs.trust.slave.core.service.action.utxo.UTXOActionHandler UTXOActionHandler;

    //数据驱动
    @DataProvider
    public Object[][] provideNumbers(Method method){
        String path = JsonFileUtil.findJsonFile("java/com/higgs/trust/slave/core/service/action/utxo");
        HashMap<String, String>[][] arrmap = (HashMap<String, String>[][]) JsonFileUtil.jsonFileToArry(path);
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
       // UTXOAction action = JSON.parseObject(obj.get("utxoAction").toString(),UTXOAction.class);
    }

    @Test
    public void testSpring(){
        DataBaseManager dataBaseManager = new DataBaseManager();
        dataBaseManager.executeSingleInsert("INSERT INTO `trust`.`merkle_node` (`id`, `uuid`, `node_hash`, `index`, `level`, `parent`, `tree_type`, `create_time`, `update_time`) VALUES ('17', '438661769108389891', '3fa5834dc920d385ca9b099c9fe55dcca163a6b256a261f8f147291b0e7cf633', '13', '1', '438661769108389895', 'RS', '2018-04-25 11:25:15.704', '2018-04-25 11:25:15.704');\n","jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true");
        AssertTool.isContainsExpectJsonNode("{\"index\":\"13\"}","jdbc:mysql://localhost:3306/trust?user=root&password=root&useSSL=true","SELECT * from merkle_node;");


    }

//    @Test
//    public void testPath() {
//        URL url = this.getClass().getResource("");
//        log.info("url:{}",url);
//        File file = new File(url.getFile(), "utxo");
//        log.info(file.getAbsolutePath());
//    }
}
