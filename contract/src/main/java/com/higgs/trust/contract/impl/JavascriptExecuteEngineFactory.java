package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 */
public class JavascriptExecuteEngineFactory implements ExecuteEngineFactory {

    private static final String engineName = "javascript";

    private ScriptEngine scriptEngine;

    public JavascriptExecuteEngineFactory() {

    }

    private ScriptEngine newScriptEngine(Map<String, Object> variables) {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName(ExecuteEngine.JAVASCRIPT);
        Bindings bindings = createBindings(variables);
        scriptEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

        return scriptEngine;
    }

    private Bindings createBindings(Map<String, Object> services) {
        ExecuteContext context = ExecuteContext.getCurrent();
        List<ResolverFactory> factories = new ArrayList();
        factories.add(new ServicesResolverFactory(services));
        Bindings bindings = new ScriptBindingsFactory(factories).createBindings(context);
        return bindings;
    }

    @Override public String getEngineName() {
        return engineName;
    }

    @Override public ExecuteEngine getExecuteEngine(String code, Map<String, Object> variables) {
        JavascriptExecuteEngine engine = new JavascriptExecuteEngine(newScriptEngine(variables), code);
        return engine;
    }
}
