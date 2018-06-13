package com.higgs.trust.contract;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * contract runtime Context
 *
 * @author duhongming
 */
@Slf4j public final class ExecuteContext {

    private static final ThreadLocal<ExecuteContext> currentExecuteContext = new ThreadLocal();

    private ContractEntity contract;
    private String stateInstanceKey;
    private ContractStateStore stateStore;
    private ExecuteContextData contextData;

    private ExecuteContext() {
        currentExecuteContext.set(this);
    }

    public static ExecuteContext getCurrent() {
        ExecuteContext context = currentExecuteContext.get();
        return context;
    }

    public static ExecuteContext newContext() {
        ExecuteContext context = new ExecuteContext();
        return context;
    }

    public static ExecuteContext newContext(ExecuteContextData data) {
        ExecuteContext context = newContext();
        context.contextData = data;
        return context;
    }

    public static void Clear() {
        currentExecuteContext.remove();
    }

    public Object getData(String name) {
        return this.contextData.get(name);
    }

    public ContractEntity getContract() {
        return this.contract;
    }

    public ExecuteContext setContract(ContractEntity contract) {
        this.contract = contract;
        return this;
    }

    public String getStateInstanceKey() {
        return StringUtils.isEmpty(this.stateInstanceKey) ? this.contract.getAddress() : this.stateInstanceKey;
    }

    public ExecuteContext setStateInstanceKey(String instanceKey) {
        this.stateInstanceKey = instanceKey;
        return this;
    }

    public ContractStateStore getStateStore() {
        return stateStore;
    }

    public void setDbStateStore(ContractStateStore stateStore) {
        this.stateStore = stateStore;
    }

    public ExecuteContextData getContextData() {
        return contextData;
    }

    public <T extends ExecuteContextData> T getContextData(Class<T> tClazz) {
        return (T) contextData;
    }

    public static void require(Object self, Boolean isRequired, String message){
        if (!isRequired) {
            throw new SmartContractException(message);
        }
    }

    public static void exception(Object self, String message) {
        throw new SmartContractException(message);
    }

    public static Logger getLogger() {
        return log;
    }
}
