package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.*;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;

/**
 * @author duhongming
 * @date 2018/04/25
 */
@Slf4j public class JavascriptExecuteEngine implements ExecuteEngine {

    private static final String DB_STATE_CTX_KEY_NAME = "db";

    private ScriptEngine scriptEngine;
    private String code;

    public JavascriptExecuteEngine(ScriptEngine scriptEngine, String code) {
        this.scriptEngine = scriptEngine;
        this.code = code;
    }

    @Override public Object execute(String methodName, Object... bizArgs) {
        long startTime = System.currentTimeMillis();
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
                StateManager stateManager = new StateManager(context,dbStateStore);
                bindings.put(DB_STATE_CTX_KEY_NAME, stateManager);
            }

            compiledScript.eval(bindings);
            ScriptObjectMirror method = (ScriptObjectMirror) bindings.get(methodName);
            if (null == method) {
                log.error("method: {} not found", methodName);
                throw new SmartContractException(String.format("method: %s not found", methodName));
            }

            Object result = method.call(null, bizArgs);
            if (dbStateStore != null) {
                StateManager state = (StateManager) bindings.get(DB_STATE_CTX_KEY_NAME);
                state.flush();
            }
            return result;
        } catch (ScriptException ex) {
            ex.printStackTrace();
            log.error("ScriptException", ex);
            throw new SmartContractException(ex.getMessage());
        } finally {
            log.info("execute contract duration: {}", System.currentTimeMillis() - startTime);
            ExecuteContext.Clear();
        }
    }
}
