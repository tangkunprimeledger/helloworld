package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.ExecuteConfig;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineFactory;

import java.util.Map;

/**
 * @author duhongming
 * @date 2018/6/6
 */
public class RhinoExecuteEngineFactory implements ExecuteEngineFactory {

    private static final String engineName = "javascript";

    static {
        TrustContextFactory.install();
    }

    public RhinoExecuteEngineFactory() {

    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public ExecuteEngine createExecuteEngine(String code, Map<String, Object> variables, ExecuteConfig executeConfig) {
        return new RhinoExecuteEngine(code, variables, executeConfig);
    }
}
