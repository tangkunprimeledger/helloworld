package com.higgs.trust.contract;

import java.util.Map;

public interface ExecuteEngineFactory {
    /**
     * Returns the full  name of the <code>ExceuteEngine</code>.
     *
     * @return The name of the engine implementation.
     */
    public String getEngineName();

    /**
     * Returns the instance of ExceuteEngine implementation
     *
     * @param code
     * @param variables
     * @return
     */
    public ExecuteEngine getExecuteEngine(String code, Map<String, Object> variables);
}
