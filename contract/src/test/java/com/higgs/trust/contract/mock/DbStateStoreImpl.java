package com.higgs.trust.contract.mock;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.StateManager;

import java.util.HashMap;
import java.util.Map;

public class DbStateStoreImpl implements ContractStateStore {

    private static Map<String, Map<String,Object>> db = new HashMap<>();

    @Override public void put(String key, Map<String,Object> state) {
        System.out.println("[put]key:" + key);
        db.put(key, state);
        System.out.println("[put]state:" + JSON.toJSONString(state));
    }


    @Override public Map<String,Object> get(String key) {
        System.out.println("[get]key:" + key);
        //return db.get(key);
        //language=JSON
        String json = "{\"gameConfig\":{\"amount\":10,\"countOfDay\":5},\"key-lottery-user-user01\":{\"lotteryDate\":1541751816703,\"lotteryCount\":10,\"user\":\"user01\"},\"awards\":[{\"amount\":100,\"level\":1.0,\"probability\":0.01,\"count\":-1.0},{\"amount\":50,\"level\":2,\"probability\":0.1,\"count\":9},{\"amount\":30,\"level\":3,\"probability\":0.2,\"count\":-1},{\"amount\":10,\"level\":4,\"probability\":0.6,\"count\":-1},{\"amount\":0,\"level\":5,\"probability\":0.8,\"count\":10}],\"txId123\":{\"msg\":\"SUCCESS\",\"data\":{\"isWinner\":true,\"random\":91004,\"txId\":\"txId123\",\"user\":\"user01\",\"winnerAward\":{\"amount\":50,\"level\":2.0}},\"success\":true}}";
        //        String json = "{\"awards\":[{\"amount\":100,\"count\":-1.0,\"level\":1.0,\"probability\":0.01},{\"amount\":50,\"count\":10,\"level\":2,\"probability\":0.1},{\"amount\":30,\"count\":-1,\"level\":3,\"probability\":0.2},{\"amount\":10,\"count\":-1,\"level\":4,\"probability\":0.6},{\"amount\":0,\"count\":10,\"level\":5,\"probability\":0.8}],\"gameConfig\":{\"amount\":10,\"countOfDay\":5}}\n";
        return JSON.parseObject(json);
    }

    @Override public void remove(String key) {
        db.remove(key);
    }
}
