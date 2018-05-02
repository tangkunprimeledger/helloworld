package com.higgs.trust.contract.impl;

import com.higgs.trust.contract.ExecuteContext;
import com.higgs.trust.contract.ExecuteEngine;
import com.higgs.trust.contract.ExecuteEngineFactory;
import com.higgs.trust.contract.SmartContractException;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.options.Options;
import lombok.extern.slf4j.Slf4j;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 */
@Slf4j public class JavascriptExecuteEngineFactory implements ExecuteEngineFactory {

    private static final String engineName = "javascript";

    private ScriptEngine scriptEngine;

    public JavascriptExecuteEngineFactory() {

    }

    private static ClassLoader getAppClassLoader() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        return ccl == null ? JavascriptExecuteEngineFactory.class.getClassLoader() : ccl;
    }

    private static MethodType type(Class<?> returnType, Class... paramTypes) {
        MethodType mt = MethodType.methodType(returnType, paramTypes);
        return mt;
    }

    private ScriptEngine newScriptEngine(Map<String, Object> variables) {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName(ExecuteEngine.JAVASCRIPT);
        Bindings bindings = createBindings(variables);
        Context context = new Context(new Options("nashorn"), new ErrorManager(), getAppClassLoader());
        Context.setGlobal(context.createGlobal());
        try {
            MethodHandle requireMethod = MethodHandles.lookup().findStatic(ExecuteContext.class, "require", type(void.class, Object.class, Boolean.class, String.class));
            MethodHandle exceptionMethod = MethodHandles.lookup().findStatic(ExecuteContext.class, "exception", type(void.class, Object.class, String.class));
            bindings.put("require", ScriptFunction.createBuiltin("require", requireMethod));
            bindings.put("exception", ScriptFunction.createBuiltin("exception", exceptionMethod));
        } catch (NoSuchMethodException e) {
            log.warn(e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.warn(e.getMessage());
            e.printStackTrace();
        }

        scriptEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

        return scriptEngine;
    }

    private Bindings createBindings(Map<String, Object> services) {
        ExecuteContext context = ExecuteContext.getCurrent();
        List<ResolverFactory> factories = new ArrayList();
        factories.add(new ServicesResolverFactory(services));
        Bindings bindings = new ScriptBindingsFactory(factories).createBindings(context);
        bindings.put("log", ExecuteContext.getLogger());
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
