package com.higgs.trust.contract;

public class ContractApiService {

    private ExecuteContext context;

    public ContractApiService() {
    }

    protected ExecuteContext getContext() {
        if (context == null) {
            context = ExecuteContext.getCurrent();
        }
        return context;
    }

    public ExecuteContextData getContextData() {
        return getContext().getContextData();
    }

    public <T extends ExecuteContextData> T getContextData(Class<T> tClazz) {
        return getContext().getContextData(tClazz);
    }
}
