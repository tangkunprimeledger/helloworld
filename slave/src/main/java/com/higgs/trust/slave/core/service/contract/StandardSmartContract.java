package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.contract.*;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.ContractException;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.Contract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SmartContract
 * @author duhongming
 * @date 2018-04-20
 */
@Slf4j @Service public class StandardSmartContract {

    @Autowired
    private StandardContractContextService contextService;
    @Autowired
    private ContractSnapshotAgent snapshotAgent;
    @Autowired
    private ContractStateSnapshotAgent contractStateSnapshotAgent;

    private ExecuteEngineManager engineManager;

    private ExecuteEngineManager getExecuteEngineManager() {
        if (null != engineManager) {
            return engineManager;
        }

        ExecuteConfig executeConfig = new ExecuteConfig();
        executeConfig.setInstructionCountQuota(100000000);
        executeConfig.allow(StandardContractContextService.class);

        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", contextService);
        manager.setExecuteConfig(executeConfig);
        manager.setDbStateStore(new ContractStateStore() {
            @Override
            public void put(String key, Object state) {
                log.debug("put contract state to db, the key is {}, state: {}", key, state);
                contractStateSnapshotAgent.put(key, state);
            }
            @Override
            public Object get(String key) {
                return contractStateSnapshotAgent.get(key);
            }

            @Override
            public void remove(String key) {
                contractStateSnapshotAgent.remove(key);
            }
        });
        engineManager = manager;
        return engineManager;
    }

    private Contract getContract(String address) {
        Contract contract = snapshotAgent.get(address);
        if (null == contract) {
            log.error("contract not fond: {}", address);
            throw new ContractException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR, String.format("contract not fond: %s", address));
        }
        return contract;
    }

    private Object execute(Contract contract, String methodName, Object... args) {
        ExecuteEngineManager manager = getExecuteEngineManager();
        ExecuteEngine engine = manager.getExecuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
        Object result = engine.execute(methodName, args);
        return result;
    }

    private Object execute(String address, String instanceId, ExecuteContextData data, Object... args) {
        Contract contract = getContract(address);
        ExecuteContext context = ExecuteContext.newContext(data);
        context.setStateInstanceKey(instanceId);
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setAddress(contract.getAddress());
        context.setContract(contractEntity);

        Object result = execute(contract, "main", args);
        return result;
    }

    public Object execute(String address, ExecuteContextData data, Object... args) {
        try {
            Profiler.enter(String.format("execute contract at %s", address));
            Object result = execute(address, address, data, args);
            return result;
        } finally {
            Profiler.release();
        }
    }

    public Object executeQuery(String address, String methodName, Object... args) {
        try {
            Profiler.enter(String.format("query contract at %s", address));
            Contract contract = getContract(address);
            ExecuteContext context = ExecuteContext.newContext();
            context.setStateInstanceKey(address);
            ContractEntity contractEntity = new ContractEntity();
            contractEntity.setAddress(contract.getAddress());
            context.setContract(contractEntity);
            context.setOnlyQuery(true);

            Object result = execute(contract, methodName, args);
            return result;
        } finally {
            Profiler.release();
        }
    }

    public Object execute(AccountContractBinding binding, ExecuteContextData data) {
        Profiler.enter(String.format("execute contract at %s", binding.getContractAddress()));
        try {
            if (binding == null) {
                log.warn("binding is null");
                return null;
            }
            Object args = com.alibaba.fastjson.JSON.parse(binding.getArgs());
            Object result = execute(binding.getContractAddress(), binding.getHash(), data, args);
            return result;
        } finally {
            Profiler.release();
        }
    }

    public void init(String address, Object... args) {
        Contract contract = getContract(address);
        ExecuteContext context = ExecuteContext.newContext();
        context.setTryInitialization(true);
        context.setStateInstanceKey(address);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setAddress(contract.getAddress());
        context.setContract(contractEntity);

        ExecuteEngineManager manager = getExecuteEngineManager();
        ExecuteEngine engine = manager.getExecuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
        engine.execute("init", args);
    }
}
