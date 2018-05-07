package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineManager;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.util.Profiler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j @Service public class UTXOSmartContractImpl implements UTXOSmartContract {

    @Autowired private UTXOContextService contextService;

    private ExecuteEngineManager engineManager;

    private ExecuteEngineManager getExecuteEngineManager() {
        if (null != engineManager) {
            return engineManager;
        }

        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", contextService);
        engineManager = manager;
        return engineManager;
    }

    @Override public boolean execute(String code, ExecuteContextData data, TxProcessTypeEnum processType) {
        Profiler.enter("execute utxo contract");
        try {
            ExecuteEngineManager manager = getExecuteEngineManager();
            ExecuteContext context = ExecuteContext.newContext(data);
            context.setValidateStage(processType == TxProcessTypeEnum.VALIDATE);
            ExecuteEngine engine = manager.getExecuteEngine(code, ExecuteEngine.JAVASCRIPT);
            Object result = engine.execute("verify");
            return (Boolean)result;
        } finally {
            Profiler.release();
        }
    }
}
