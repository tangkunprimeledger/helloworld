package com.higgs.trust.contract;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ExecuteEngineManager {

    public final static String DEFAULT_LANGUAGE = "javascript";

    private Map<String, Object> serviceMap;
    private ContractReader contractReader;
    private HashSet<ExceuteEngineFactory> engineFactories;
    private ContractStateStore stateStore;

    public ExecuteEngineManager() {
        serviceMap = new HashMap<>();
        engineFactories = new HashSet<>();

        this.initEngines();
    }

    private void initEngines() {
        ServiceLoader<ExceuteEngineFactory> sl = ServiceLoader.load(ExceuteEngineFactory.class);
        Iterator<ExceuteEngineFactory> itr = sl.iterator();
        try {
            while (itr.hasNext()) {
                try {
                    ExceuteEngineFactory fact = itr.next();
                    engineFactories.add(fact);
                } catch (ServiceConfigurationError err) {
                    System.err.println("ExceuteEngineManager providers.next(): " + err.getMessage());
                    continue;
                }
            }
        } catch (ServiceConfigurationError err) {
            System.err.println("ExceuteEngineManager providers.hasNext(): " + err.getMessage());
            return;
        }
    }

    private ExceuteEngineFactory getExceuteEngineByLanguage(String language) {
        for (ExceuteEngineFactory fact : engineFactories) {
            if (StringUtils.equalsIgnoreCase(fact.getEngineName(), language)) {
                return fact;
            }
        }
        return null;
    }

    public void setDbStateStore(ContractStateStore stateStore) {
        this.stateStore = stateStore;
    }

    public void registerServices(Map<String, ContractApiService> services) {
        if (services == null) {
            // TODO duhongming log
            return;
        }
        services.forEach((key, val) -> registerService(key, val));
    }

    public void registerService(String name, ContractApiService service) {
        if (StringUtils.isEmpty(name)) {
            // TODO duhongming log
            return;
        }
        if (service == null) {
            // TODO duhongming log
            return;
        }
        this.serviceMap.put(name, service);
    }

    public void registerService(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            // TODO duhongming log
            return;
        }
        if (value == null) {
            // TODO duhongming log
            return;
        }
        this.serviceMap.put(name, value);
    }

    public ExecuteEngine getExceuteEngine(String address) {
        ContractEntity contractEntity = contractReader.load(address);
        contractEntity.setAddress(address);

        ExecuteContext context = ExecuteContext.getCurrent();
        context.setContract(contractEntity);
        context.setDbStateStore(this.stateStore);

        ExceuteEngineFactory factory = getExceuteEngineByLanguage(DEFAULT_LANGUAGE);
        ExecuteEngine engine = factory.getExceuteEngine(contractEntity.getScript(), this.serviceMap);
        return engine;
    }

    public ExecuteEngine getExceuteEngine(String code, String language) {
        ExecuteContext context = ExecuteContext.getCurrent();
        context.setDbStateStore(this.stateStore);

        ExceuteEngineFactory factory = getExceuteEngineByLanguage(language);
        ExecuteEngine engine = factory.getExceuteEngine(code, this.serviceMap);
        return engine;
    }
}
