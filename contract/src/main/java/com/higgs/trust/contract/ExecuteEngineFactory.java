package com.higgs.trust.contract;

import java.util.Map;

/**
 * @author duhongming
 * @date 2018/04/25
 */
public interface ExecuteEngineFactory {
    /**
     * Returns the full  name of the <code>ExecuteEngine</code>.
     *
     * @return The name of the engine implementation.
     */
    public String getEngineName();

    /**
     * Returns the instance of ExecuteEngine implementation
     *
     * @param code
     * @param variables
     * @param executeConfig
     * @return
     */
    public ExecuteEngine createExecuteEngine(String code, Map<String, Object> variables, ExecuteConfig executeConfig);
}
