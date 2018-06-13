package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.*;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.ContractException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.contract.Contract;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SmartContract
 * @author duhongming
 * @date 2018-04-20
 */
@Slf4j @Service public class StandardSmartContract {

    @Autowired private StandardContractContextService contextService;
    @Autowired private ContractSnapshotAgent snapshotAgent;
    @Autowired private ContractStateSnapshotAgent contractStateSnapshotAgent;

    private ExecuteEngineManager engineManager;

    private ExecuteEngineManager getExecuteEngineManager() {
        if (null != engineManager) {
            return engineManager;
        }

        ExecuteConfig executeConfig = new ExecuteConfig();
        executeConfig.setInstructionCountQuota(10000);
        executeConfig.allow("com.higgs.trust.slave.core.service.contract.StandardContractContextService");

        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", contextService);
        manager.setExecuteConfig(executeConfig);
        manager.setDbStateStore(new ContractStateStore() {
            @Override
            public void put(String key, StateManager state) {
                log.debug("put contract state to db, the key is {}, state size: {}", key, state.getState().size());
                contractStateSnapshotAgent.put(key, state);
            }
            @Override
            public StateManager get(String key) {
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

    private Object execute(String address, String instanceId, ExecuteContextData data, Object... args) {
        Contract contract = snapshotAgent.get(address);
        if (null == contract) {
            log.error("contract not fond: {}", address);
            throw new ContractException(SlaveErrorEnum.SLAVE_CONTRACT_NOT_EXIST_ERROR, String.format("contract not fond: %s", address));
        }
        ExecuteEngineManager manager = getExecuteEngineManager();
        ExecuteContext context = ExecuteContext.newContext(data);
        context.setStateInstanceKey(instanceId);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setAddress(contract.getAddress());

        context.setContract(contractEntity);
//        context.setValidateStage(processType == TxProcessTypeEnum.VALIDATE);

        ExecuteEngine engine = manager.getExecuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
        Object result = engine.execute("main", args);
        return result;
    }

    public Object execute(String address, ExecuteContextData data, Object... args) {
        try {
            Profiler.enter(String.format("execute contract at %s", address));
            return execute(address, address, data, args);
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
            return execute(binding.getContractAddress(), binding.getHash(), data, args);
        } finally {
            Profiler.release();
        }
    }
}
