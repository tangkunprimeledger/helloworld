package com.higgs.trust.contract.mock;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;

import java.util.HashMap;
import java.util.Map;

public class DbStateStoreImpl implements ContractStateStore {

    private static Map<String, StateManager> db = new HashMap<>();

    @Override public void put(String key, StateManager state) {
        System.out.println("key:" + key);
        db.put(key, state);
        System.out.println(JSON.toJSONString(state.getState()));
    }


    @Override public StateManager get(String key) {
        //return db.get(key);
        //language=JSON
        String json = "{\"amount\":200.0,\"map\":{\"id\":1,\"name\":\"jack\"}, \"aa\": 100000000000000000, \"ming\": {\"a\": 1, \"b\": 2}}";
        Map<String, Object> map = JSON.parseObject(json);
        return new StateManager(map);
    }

    @Override public void remove(String key) {
        db.remove(key);
    }
}
