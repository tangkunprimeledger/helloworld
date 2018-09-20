package com.higgs.trust.contract.rhino.function;

import com.higgs.trust.contract.rhino.SafeNativeJavaObject;
import com.higgs.trust.contract.rhino.types.BigDecimalWrap;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author duhongming
 * @date 2018/9/10
 */
public class MathNativeFunction {

    protected static AddFunction ADD_FUNCTION = new AddFunction();
    protected static SubtractFunction SUBTRACT_FUNCTION = new SubtractFunction();
    protected static MultiplyFunction MULTIPLY_FUNCTION = new MultiplyFunction();
    protected static DivideFunction DIVIDE_FUNCTION = new DivideFunction();
    protected static EqualsFunction EQUALS_FUNCTION = new EqualsFunction();
    protected static CompareFunction COMPARE_FUNCTION = new CompareFunction();


    public static BigDecimal toBigDecimal(Object obj) {
        if (obj instanceof SafeNativeJavaObject) {
            obj = ((SafeNativeJavaObject) obj).unwrap();
        }
        if (obj instanceof BigDecimalWrap) {
            return ((BigDecimalWrap) obj).getRawBigDecimal();
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Double) {
            return new BigDecimal((Double) obj);
        }
        if (obj instanceof BigInteger) {
            return new BigDecimal((BigInteger) obj);
        }
        if (obj instanceof Integer) {
            return new BigDecimal((Integer) obj);
        }

        return new BigDecimal(obj.toString());
    }

    private static class AddFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            BigDecimal result = toBigDecimal(args[0]).add(toBigDecimal(args[1]));
            if (args.length > 2) {

                for(int i = 2; i < args.length; i++) {
                    result = result.add(toBigDecimal(args[i]));
                }
            }
            return result;
        }
    }

    private static class SubtractFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            BigDecimal result = toBigDecimal(args[0]).subtract(toBigDecimal(args[1]));
            if (args.length > 2) {
                for(int i = 2; i < args.length; i++) {
                    result = result.subtract(toBigDecimal(args[i]));
                }
            }
            return result;
        }
    }

    private static class MultiplyFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            BigDecimal result = toBigDecimal(args[0]).multiply(toBigDecimal(args[1]));
            if (args.length > 2) {
                for(int i = 2; i < args.length; i++) {
                    result = result.multiply(toBigDecimal(args[i]));
                }
            }
            return result;
        }
    }

    private static class DivideFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            BigDecimal result = toBigDecimal(args[0]).divide(toBigDecimal(args[1]));
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    result = result.divide(toBigDecimal(args[i]));
                }
            }
            return result;
        }
    }

    private static class EqualsFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return toBigDecimal(args[0]).equals(toBigDecimal(args[1]));
        }
    }

    private static class CompareFunction extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return toBigDecimal(args[0]).compareTo(toBigDecimal(args[1]));
        }
    }

}
