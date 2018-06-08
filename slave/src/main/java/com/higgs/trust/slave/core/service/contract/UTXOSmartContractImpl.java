package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineManager;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.contract.Contract;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j @Service public class UTXOSmartContractImpl implements UTXOSmartContract {

    @Autowired private UTXOContextService contextService;
    @Autowired private ContractRepository contractRepository;
    @Autowired private ContractSnapshotAgent contractSnapshotAgent;

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

    private Contract queryContract(String address, TxProcessTypeEnum processType) {
        Contract contract = processType == TxProcessTypeEnum.VALIDATE
                ? contractSnapshotAgent.get(address)
                : contractRepository.queryByAddress(address);
        return contract;
    }

    @Override
    public boolean isExist(String address, TxProcessTypeEnum processType) {
        Contract contract = queryContract(address, processType);
        return  contract != null;
    }

    @Override public boolean execute(String address, ExecuteContextData contextData, TxProcessTypeEnum processType) {
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("argument code is empty");
        }
        if(contextData == null) {
            throw new IllegalArgumentException("contextData is null");
        }
        if (processType == null) {
            throw new IllegalArgumentException("processType is null");
        }

        Contract contract = queryContract(address, processType);
        if (contract == null) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }

        Profiler.enter("execute utxo contract");
        try {
            ExecuteEngineManager manager = getExecuteEngineManager();
            ExecuteContext context = ExecuteContext.newContext(contextData);
            context.setValidateStage(processType == TxProcessTypeEnum.VALIDATE);
            ExecuteEngine engine = manager.getExecuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
            Object result = engine.execute("verify");
            return (Boolean)result;
        } finally {
            Profiler.release();
        }
    }
}
