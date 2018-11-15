package com.higgs.trust.contract.rhino;

import com.higgs.trust.contract.*;
import com.higgs.trust.contract.rhino.function.DateFuncs;
import com.higgs.trust.contract.rhino.function.MathFuncs;
import com.higgs.trust.contract.rhino.function.PrintNativeFunction;
import com.higgs.trust.contract.rhino.function.SafeEvalFunction;
import com.higgs.trust.contract.rhino.types.JsDate;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/6/6
 */
@Slf4j
public class RhinoExecuteEngine implements ExecuteEngine {

    private static final String INITIALIZATION_METHOD_NAME = "init";
    private static final String DB_STATE_CTX_KEY_NAME = "db";
    private static Map<String, Script> scriptCache = new HashMap<>();

    private String code;
    private Map<String, Object> variables;
    private ExecuteConfig executeConfig;

    public RhinoExecuteEngine(String code, Map<String, Object> variables, ExecuteConfig executeConfig) {
        this.code = code;
        this.variables = variables;
        this.executeConfig = executeConfig;
    }

    @Override
    public Object execute(String methodName, Object... bizArgs) {
        Object result = executeInternal(methodName, bizArgs);
        return result;
    }

    private Object executeInternal(String methodName, Object... bizArgs) {
        if (INITIALIZATION_METHOD_NAME.equals(methodName) && !ExecuteContext.getCurrent().isTryInitialization()) {
            throw new SmartContractException("can't invoke init method");
        }
        //Context context = Context.enter();
        long startTime = System.currentTimeMillis();
        Context context = new TrustContextFactory(executeConfig).enterContext();
        context.setLanguageVersion(Context.VERSION_ES6);
        context.setWrapFactory(new SafeWrapFactory());
        context.setClassShutter(new SafeClassShutter(executeConfig.getAllowedClasses()));
        System.out.println(context.getLanguageVersion());
        try {
            ScriptableObject scope = context.initStandardObjects();
            ScriptableObject.defineClass(scope, JsDate.class);
            Script script = scriptCache.get("" + code.hashCode());
            if (script == null) {
                try {
                    script = context.compileString(code, "contract", 1, null);
                } catch (EvaluatorException ex) {
                    throw new ContractSyntaxException("syntax error:" + ex.getMessage());
                }
                scriptCache.put("" + code.hashCode(), script);
            }

            applyVariables(scope);
            script.exec(context, scope);
            Object func = scope.get(methodName, scope);
            if (func == null || !(func instanceof Function)) {
                if (ExecuteContext.getCurrent().isTryInitialization()) {
                    log.debug("try to initialization contract, but init method not find");
                    return null;
                }
                throw new SmartContractException(String.format("method %s not find", methodName));
            }
            Object result = ((Function) func).call(context, scope, scope, bizArgs);

            ExecuteContext executeContext = ExecuteContext.getCurrent();
            ContractStateStore dbStateStore = executeContext.getStateStore();
            if (dbStateStore != null && !ExecuteContext.getCurrent().isOnlyQuery()) {
                StateManager state = (StateManager) scope.get(DB_STATE_CTX_KEY_NAME);
                state.flush();
            }
            if (result instanceof NativeJavaObject) {
                return ((NativeJavaObject) result).unwrap();
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SmartContractException(ex.getMessage());
        } finally {
            Context.exit();
            ExecuteContext.Clear();
            log.info("execute contract duration: {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void applyVariables(ScriptableObject scope) {
        if (this.variables != null) {
            this.variables.forEach((key, val) -> {
                scope.put(key, scope, val);
            });
        }

        scope.defineProperty("print", new PrintNativeFunction(), ScriptableObject.DONTENUM);
        scope.defineProperty("eval", new SafeEvalFunction(), ScriptableObject.DONTENUM);
        scope.put("math", scope, MathFuncs.getInstance());
        scope.put("date", scope, DateFuncs.getInstance());

        ExecuteContext context = ExecuteContext.getCurrent();
        ContractStateStore dbStateStore = context.getStateStore();
        if (dbStateStore != null) {
            StateManager stateManager = new StateManager(context,dbStateStore);
            scope.put(DB_STATE_CTX_KEY_NAME, scope, stateManager);
        }
    }
}
