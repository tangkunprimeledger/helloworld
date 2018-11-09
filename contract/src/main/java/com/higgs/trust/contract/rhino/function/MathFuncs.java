package com.higgs.trust.contract.rhino.function;

import org.mozilla.javascript.BaseFunction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/7/17
 */
public class MathFuncs {

    private static MathFuncs instance = new MathFuncs();

    private static Map<String, BaseFunction> funcMapping = new HashMap<>();

    static {
        funcMapping.put("add", MathNativeFunction.ADD_FUNCTION);
        funcMapping.put("subtract", MathNativeFunction.SUBTRACT_FUNCTION);
        funcMapping.put("multiply", MathNativeFunction.MULTIPLY_FUNCTION);
        funcMapping.put("divide", MathNativeFunction.DIVIDE_FUNCTION);
        funcMapping.put("eq", MathNativeFunction.EQUALS_FUNCTION);
        funcMapping.put("compare", MathNativeFunction.COMPARE_FUNCTION);
    }

    public static BaseFunction getFuncByName(String funcName) {
        BaseFunction func = funcMapping.get(funcName);
        return func;
    }

    public static MathFuncs getInstance() {
        return instance;
    }

    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal TEN = BigDecimal.TEN;

    public BaseFunction add = MathNativeFunction.ADD_FUNCTION;
    public BaseFunction subtract = MathNativeFunction.SUBTRACT_FUNCTION;
    public BaseFunction multiply = MathNativeFunction.MULTIPLY_FUNCTION;
    public BaseFunction divide = MathNativeFunction.DIVIDE_FUNCTION;
    public BaseFunction eq = MathNativeFunction.EQUALS_FUNCTION;
    public BaseFunction compare = MathNativeFunction.COMPARE_FUNCTION;
}
