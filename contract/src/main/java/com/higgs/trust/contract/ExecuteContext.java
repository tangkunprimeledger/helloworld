package com.higgs.trust.contract;

import org.apache.commons.lang3.StringUtils;

/**
 * contract runtime Context
 *
 * @author duhongming
 */
public final class ExecuteContext {

    private static final ThreadLocal<ExecuteContext> currentExceuteContext = new ThreadLocal();

    private ContractEntity contract;
    private String instanceAddress;
    private ContractStateStore stateStore;
    private ExecuteContextData contextData;
    private boolean validateStage ;

    private ExecuteContext() {
        currentExceuteContext.set(this);
    }

    public static ExecuteContext getCurrent() {
        ExecuteContext context = currentExceuteContext.get();
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
        currentExceuteContext.remove();
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

    public String getInstanceAddress() {
        return StringUtils.isEmpty(this.instanceAddress) ? this.contract.getAddress() : this.instanceAddress;
    }

    public ExecuteContext setInstanceAddress(String instanceAddress) {
        this.instanceAddress = instanceAddress;
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

    public boolean isValidateStage() {
        return validateStage;
    }

    public void setValidateStage(boolean validateStage) {
        this.validateStage = validateStage;
    }
}
