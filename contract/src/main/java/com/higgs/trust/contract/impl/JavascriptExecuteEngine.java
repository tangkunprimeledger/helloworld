package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ContractStateStore;
import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.StateManager;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;

public class JavascriptExecuteEngine implements ExecuteEngine {

    private static final String dbStateKeyName = "db";

    private ScriptEngine scriptEngine;
    private String code;

    public JavascriptExecuteEngine(ScriptEngine scriptEngine, String code) {
        this.scriptEngine = scriptEngine;
        this.code = code;
    }

    @Override public Object execute(String methodName, Object... bizArgs) {
        ScriptEngine engine = scriptEngine;
        ExecuteContext context = ExecuteContext.getCurrent();
        try {
            Compilable compilable = (Compilable) engine;
            CompiledScript compiledScript = compilable.compile(code);
            Bindings bindings = scriptEngine.createBindings();
            context.getContextData().keySet().forEach(key -> {
                bindings.put(key, context.getData(key));
            });

            ContractStateStore dbStateStore = context.getStateStore();
            if (dbStateStore != null) {
                StateManager stateManager = dbStateStore.get(context.getInstanceAddress());
                if (stateManager == null) {
                    stateManager = new StateManager();
                }
                bindings.put(dbStateKeyName, stateManager);
            }

            compiledScript.eval(bindings);
            ScriptObjectMirror method = (ScriptObjectMirror) bindings.get(methodName);
            if (null == method) {
                System.out.println("method " + methodName + " not find");
                // TODO [duhongming] no method find
            }

            Object result = method.call(null, bizArgs);
            if (dbStateStore != null) {
                StateManager state = (StateManager) bindings.get(dbStateKeyName);
                dbStateStore.put(context.getInstanceAddress(), state);
            }
            return result;
        } catch (ScriptException ex) {
            // TODO duhongming to handle ScriptException
            ex.printStackTrace();
            return null;
        } finally {
            ExecuteContext.Clear();
        }
    }
}
