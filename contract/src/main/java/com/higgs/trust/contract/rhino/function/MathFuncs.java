package com.higgs.trust.contract.rhino.function;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author duhongming
 * @date 2018/7/17
 */
public class MathFuncs {

    private static MathFuncs instance = new MathFuncs();

    public static MathFuncs getInstance() {
        return instance;
    }

    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal TEN = BigDecimal.TEN;

    public BigDecimal add(Object x, Object y) {
        return getBigDecimal(x).add(getBigDecimal(y));
    }

    public BigDecimal subtract(Object x, Object y) {
        return getBigDecimal(x).subtract(getBigDecimal(y));
    }

    public BigDecimal multiply(Object x, Object y) {
        return getBigDecimal(x).multiply(getBigDecimal(y));
    }

    public BigDecimal divide(Object x, Object y) {
        return getBigDecimal(x).divide(getBigDecimal(y));
    }

    public boolean eq(Object x, Object y) {
        return getBigDecimal(x).equals(getBigDecimal(y));
    }

    public int compareTo(Object x, Object y) {
        return getBigDecimal(x).compareTo(getBigDecimal(y));
    }

    public static BigDecimal getBigDecimal(Object obj) {
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
}
