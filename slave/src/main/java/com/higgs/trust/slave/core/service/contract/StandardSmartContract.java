package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.*;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.Contract;
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
    @Autowired private DbContractStateStoreImpl contractStateStore;

    private ExecuteEngineManager engineManager;

    private ExecuteEngineManager getExceuteEngineManager() {
        if (null != engineManager) {
            return engineManager;
        }
        ExecuteEngineManager manager = new ExecuteEngineManager();
        manager.registerService("ctx", contextService);
        manager.setDbStateStore(new ContractStateStore() {
            private ContractStateStore getContractStateStore() {
                if (ExecuteContext.getCurrent().isValidateStage()) {
                    return contractStateSnapshotAgent;
                }
                return contractStateStore;
            }
            @Override
            public void put(String key, StateManager state) {
                getContractStateStore().put(key, state);
            }
            @Override
            public StateManager get(String key) {
                return getContractStateStore().get(key);
            }
            @Override
            public void remove(String key) {
                getContractStateStore().remove(key);
            }
        });
        engineManager = manager;
        return engineManager;
    }

    private Object execute(String address, String instanceId, ExecuteContextData data, TxProcessTypeEnum processType, Object... args) {
        Contract contract = snapshotAgent.get(address);
        if (null == contract) {
            log.error("contract not fond: {}", address);
            // TODO [duhongming] throws Exception
            return null;
        }
        ExecuteEngineManager manager = getExceuteEngineManager();
        ExecuteContext context = ExecuteContext.newContext(data);
        context.setInstanceAddress(instanceId);
        context.setValidateStage(processType == TxProcessTypeEnum.VALIDATE);
        ExecuteEngine engine = manager.getExceuteEngine(contract.getCode(), ExecuteEngine.JAVASCRIPT);
        Object result = engine.execute("main", args);
        return result;
    }

    public Object execute(String address, ExecuteContextData data, TxProcessTypeEnum processType, Object... args) {
        return execute(address, address, data, processType, args);
    }

    public Object execute(AccountContractBinding binding, ExecuteContextData data, TxProcessTypeEnum processType) {
        if (binding == null) {
            log.error("binding is null");
        }
        Object args = com.alibaba.fastjson.JSON.parse(binding.getArgs());
        return execute(binding.getContractAddress(), binding.getHash(), data, processType, args);
    }
}
