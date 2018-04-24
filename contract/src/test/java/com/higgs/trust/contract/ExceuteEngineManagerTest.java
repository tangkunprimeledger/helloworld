package com.higgs.trust.contract;

import com.higgs.trust.contract.impl.ExecuteContextDataImpl;
import com.higgs.trust.contract.mock.DbStateStoreImpl;
import com.higgs.trust.contract.mock.Person;
import com.higgs.trust.contract.mock.ShareContextSerivce;
import org.junit.Test;

import java.util.Map;

public class ExceuteEngineManagerTest extends BaseTest {

    private ExecuteEngineManager getExceuteEngineManager() {
        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", new ShareContextSerivce());
        manager.setDbStateStore(new DbStateStoreImpl());
        return manager;
    }

    private ExecuteContextData newContextData() {
        return ExecuteContextDataImpl.newContextData();
    }

    private ExecuteContextData newContextData(Map<String, Object> data) {
        return ExecuteContextDataImpl.newContextData(data);
    }

    @Test public void testExceuteContractByAddress() {
        ExecuteEngineManager manager = getExceuteEngineManager();
        String address = "d3cce84d0800172d09c88ccad61130611bd047a4";
        Object result = manager.getExceuteEngine(address).execute("verify", 1, 2, 3);
    }

    @Test public void testExceuteContractByCode() {
        ExecuteEngineManager manager = getExceuteEngineManager();
        String code = loadCodeFromResourceFile("/case2.js");
        ExecuteContextData contextData = newContextData().put("admin", new Person("zhangs", 30));
        ExecuteContext.newContext(contextData).setInstanceAddress("0xddkdkadJAkdkdkkdkdd");
        Object result = manager.getExceuteEngine(code, "javascript").execute("verify", 1, 2);
        System.out.println(result.toString());
    }
}
