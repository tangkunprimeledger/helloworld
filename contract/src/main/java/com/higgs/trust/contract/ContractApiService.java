package com.higgs.trust.contract;

public class ContractApiService {

    public ContractApiService() {
    }

    protected ExecuteContext getContext() {
        return ExecuteContext.getCurrent();
    }

    public ExecuteContextData getContextData() {
        return getContext().getContextData();
    }

    public <T extends ExecuteContextData> T getContextData(Class<T> tClazz) {
        return getContext().getContextData(tClazz);
    }
}
