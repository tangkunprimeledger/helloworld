package com.higgs.trust.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.impl.ExecuteContextDataImpl;
import com.higgs.trust.contract.mock.DbStateStoreImpl;
import com.higgs.trust.contract.mock.Person;
import com.higgs.trust.contract.mock.ShareContextSerivce;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

public class ExceuteEngineManagerTest extends BaseTest {

    private ExecuteEngineManager getExceuteEngineManager() {
        ExecuteConfig executeConfig = new ExecuteConfig();
        executeConfig.setInstructionCountQuota(10000);
        executeConfig.allow("com.higgs.trust.contract.mock.ShareContextSerivce")
            .allow("com.higgs.trust.contract.mock.ShareBlockSerivce")
            .allow("com.higgs.trust.contract.mock.Person")
            .allow("com.higgs.trust.contract.mock.Colors")
            .allow("java.lang.Class")
            .allow(BigDecimal.class);
        ExecuteConfig.DEBUG = true;
        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", new ShareContextSerivce());
        manager.setDbStateStore(new DbStateStoreImpl());
        manager.setExecuteConfig(executeConfig);
        return manager;
    }

    private ExecuteContextData newContextData() {
        return ExecuteContextDataImpl.newContextData();
    }

    private ExecuteContextData newContextData(Map<String, Object> data) {
        return ExecuteContextDataImpl.newContextData(data);
    }

    @Test public void testExceuteContractByCode() {
        ExecuteEngineManager manager = getExceuteEngineManager();
        String code = loadCodeFromResourceFile("/case2.js");
        ExecuteContextData contextData = newContextData().put("admin", new Person("zhangs", 30));
        ExecuteContext.newContext(contextData).setStateInstanceKey("0xddkdkadJAkdkdkkdkdd");
        Object result = manager.getExecuteEngine(code, "rhino").execute("verify", 1, 2);
        System.out.println(result.toString());
    }

    @Test public void testExceuteContractByCode3() {
        ExecuteEngineManager manager = getExceuteEngineManager();
        String code = loadCodeFromResourceFile("/case3.js");
        ExecuteContextData contextData = newContextData().put("admin", new Person("zhangs", 30));
        ExecuteContext.newContext(contextData).setStateInstanceKey("0xddkdkadJAkdkdkkdkdd");

        String bizArgsJson = "[\"add\", [1,2]]";
        Object[] bizArgs = JSON.parseArray(bizArgsJson).toArray();
        Object result = manager.getExecuteEngine(code, "javascript").execute("main", bizArgs);
        System.out.println(result.toString());
    }

    @Test public void testDemo() {
        String code = "function print() {  } " +
                "function sayHello() {print('hello')} " +
                "function main() { sayHello()}";

        ExecuteEngineManager manager = getExceuteEngineManager();
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            ExecuteContextData contextData = newContextData();
            ExecuteContext.newContext(contextData).setStateInstanceKey("0xddkdkadJAkdkdkkdkdd");


            Object result = manager.getExecuteEngine(code, "javascript").execute("main");
            //System.out.println(result.toString());
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }


    @Test public void testAward() {
        ExecuteEngineManager manager = getExceuteEngineManager();
        String code = loadCodeFromResourceFile("file:/Users/liuyu/IdeaProjects/trust-gsp/trust-gsp/src/main/resources/award.js");
        ExecuteContextData contextData = newContextData().put("admin", new Person("zhangs", 30));
        ExecuteContext.newContext(contextData).setStateInstanceKey("0xddkdkadJAkdkdkkdkdd");
        ExecuteEngine engine =manager.getExecuteEngine(code, "javascript");

//        Object result = engine.execute("getTotalProbability", null);

        String bizArgsJson = "{\"txId\":\"txId123\",\"user\":\"user01\",\"random\":\"91000\"}";
        Object[] bizArgs = new Object[]{JSON.parseObject(bizArgsJson)};
        Object result = engine.execute("lottery", bizArgs);

        System.out.println(result.toString());
    }
}
