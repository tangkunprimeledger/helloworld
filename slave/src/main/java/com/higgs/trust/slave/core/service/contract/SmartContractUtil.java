package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineManager;
import com.higgs.trust.contract.impl.ExecuteContextDataImpl;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;

import java.util.Map;

public final class SmartContractUtil {

    public static ExecuteContextData newContextData() {
        return ExecuteContextDataImpl.newContextData();
    }

    public static ExecuteContextData newContextData(Map<String, Object> data) {
        return ExecuteContextDataImpl.newContextData(data);
    }
}
