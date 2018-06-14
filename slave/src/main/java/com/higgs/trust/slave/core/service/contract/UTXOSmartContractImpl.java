package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.*;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.contract.Contract;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j @Service public class UTXOSmartContractImpl implements UTXOSmartContract {

    @Autowired private UTXOContextService contextService;
    @Autowired private ContractRepository contractRepository;
    @Autowired private ContractSnapshotAgent contractSnapshotAgent;

    private ExecuteEngineManager engineManager;

    private ExecuteEngineManager getExecuteEngineManager() {
        if (null != engineManager) {
            return engineManager;
        }

        ExecuteConfig executeConfig = new ExecuteConfig();
        executeConfig.setInstructionCountQuota(10000);
        executeConfig.allow(UTXOContextService.class)
            .allow(UTXO.class)
            .allow(UTXOAction.class)
            .allow(TxIn.class)
            .allow(TxOut.class)
            .allow("com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum");
        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", contextService);
        manager.setExecuteConfig(executeConfig);
        engineManager = manager;
        return engineManager;
    }

    private Contract queryContract(String address) {
        Contract contract =  contractSnapshotAgent.get(address);
        return contract;
    }

    @Override
    public boolean isExist(String address) {
        Contract contract = queryContract(address);
        return  contract != null;
    }

    @Override public boolean execute(String address, ExecuteContextData contextData) {
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("argument code is empty");
        }
        if(contextData == null) {
            throw new IllegalArgumentException("contextData is null");
        }

        Contract contract = queryContract(address);
        if (contract == null) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR);
        }

        Profiler.enter("execute utxo contract");
        try {
            ExecuteEngineManager manager = getExecuteEngineManager();
            ExecuteContext context = ExecuteContext.newContext(contextData);
            ExecuteEngine engine = manager.getExecuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
            Object result = engine.execute("verify");
            return (Boolean)result;
        } finally {
            Profiler.release();
        }
    }
}
