package com.higgs.trust.contract;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author duhongming
 */
@Slf4j public class ExecuteEngineManager {

    public final static String DEFAULT_LANGUAGE = "javascript";

    private Map<String, Object> serviceMap;
    private HashSet<ExecuteEngineFactory> engineFactories;
    private ContractStateStore stateStore;

    public ExecuteEngineManager() {
        serviceMap = new HashMap<>();
        engineFactories = new HashSet<>();

        this.initEngines();
    }

    private void initEngines() {
        log.debug("initEngines from ServiceLoader");
        ServiceLoader<ExecuteEngineFactory> sl = ServiceLoader.load(ExecuteEngineFactory.class);
        Iterator<ExecuteEngineFactory> itr = sl.iterator();
        try {
            while (itr.hasNext()) {
                try {
                    ExecuteEngineFactory fact = itr.next();
                    engineFactories.add(fact);
                } catch (ServiceConfigurationError err) {
                    log.error("ExceuteEngineManager providers.next(): {}", err.getMessage());
                    continue;
                }
            }
        } catch (ServiceConfigurationError err) {
            log.error("ExceuteEngineManager providers.hasNext(): {}", err.getMessage());
            return;
        }
    }

    private ExecuteEngineFactory getExecuteEngineByLanguage(String language) {
        for (ExecuteEngineFactory fact : engineFactories) {
            if (StringUtils.equalsIgnoreCase(fact.getEngineName(), language)) {
                return fact;
            }
        }
        log.error("ExecuteEngineFactory not found language: {}", language);
        throw new SmartContractException("ExecuteEngineFactory not found");
    }

    public void setDbStateStore(ContractStateStore stateStore) {
        this.stateStore = stateStore;
    }

    public void registerService(String name, ContractApiService service) {
        if (StringUtils.isEmpty(name)) {
            log.error("service name is empty");
            throw new SmartContractException("service name is empty");
        }
        if (service == null) {
            log.error("service is null");
            throw new SmartContractException("service is null");
        }
        this.serviceMap.put(name, service);
    }

    public ExecuteEngine getExecuteEngine(String code, String language) {
        ExecuteContext context = ExecuteContext.getCurrent();
        context.setDbStateStore(this.stateStore);

        ExecuteEngineFactory factory = getExecuteEngineByLanguage(language);
        ExecuteEngine engine = factory.getExecuteEngine(code, this.serviceMap);
        return engine;
    }
}
