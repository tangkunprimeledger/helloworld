package com.higgs.trust.contract;

import com.higgs.trust.contract.rhino.types.NativeJavaMap;

import java.util.HashMap;
import java.util.Map;

/**
 *  contract state manager
 *  @author duhongming
 */
public class StateManager {
    private ExecuteContext executeContext;
    private ContractStateStore contractStateStore;
    private Map<String, Object> state;

    public StateManager(ExecuteContext executeContext,ContractStateStore contractStateStore) {
        state = new HashMap<>(8);
        this.executeContext = executeContext;
        this.contractStateStore = contractStateStore;
    }

    public StateManager put(String name, Object value) {
        this.state.put(name, JsonHelper.clone(value));
        return this;
    }

    public Object get(String name) {
        Object obj = this.state.get(name);
        //from external store
        if(obj == null && contractStateStore != null){
            obj = contractStateStore.get(makeStateKey(name));
        }
        if (obj == null) {
            return null;
        }

        obj = JsonHelper.clone(obj);
        if (obj instanceof Map) {
            obj = new NativeJavaMap((Map)obj);
        }
        return obj;
    }

    public Map<String, Object> getState() {
        return this.state;
    }

    /**
     * flush to store
     */
    public void flush(){
        if(this.contractStateStore == null || this.state == null || this.state.isEmpty()){
            return;
        }
        Map<String,Object> keys = new HashMap<>();
        for(String key : this.state.keySet()){
            Object value = this.state.get(key);
            String newKey = makeStateKey(key);
            contractStateStore.put(newKey,(Map<String,Object>)value);
            keys.put(newKey,"1");
        }
        //save contract state`s all keys
        contractStateStore.put(executeContext.getStateInstanceKey(),keys);
    }
    /**
     * make key by contract address
     *
     * @param keyName
     * @return
     */
    private String makeStateKey(String keyName){
        if(executeContext == null){
            return null;
        }
        return executeContext.getStateInstanceKey() + "-" + keyName;
    }
}
