package com.higgs.trust.contract;

import com.higgs.trust.contract.impl.JavascriptExecuteEngineFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j public final class SmartContractExecutor implements ExecuteEngine {

    private String code;
    private String language;
    private Map<String, Object> services;
    private ExecuteEngine exceuteEngine;

    SmartContractExecutor(String code, String language, Map<String, Object> services) {
        this.code = code;
        this.language = language;
        this.services = services;
    }

    public static SmartContractExecutor newExceutor(String code, String language) {
        // SmartContractExceutor exceutor = new SmartContractExceutor();
        return null;
    }

    public static Object xecute(String code, String language, ExecuteContext context, Object... args) {

        return null;
    }

    @Override public Object execute(String method, Object... args) {
        log.debug("exceute contract: method={} args={}", method, args);
        if (exceuteEngine != null) {
            return exceuteEngine.execute(method, args);
        }
        if (StringUtils.equalsIgnoreCase("javascript", language)) {
            exceuteEngine = new JavascriptExecuteEngineFactory().getExceuteEngine(code, this.services);
        } else {
            exceuteEngine = new JavascriptExecuteEngineFactory().getExceuteEngine(code, this.services);
        }
        return exceuteEngine.execute(method, args);
    }
}
