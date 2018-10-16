package com.higgs.trust.contract.rhino.types;

import com.higgs.trust.contract.rhino.function.MathNativeFunction;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author duhongming
 * @date 2018/7/17
 */
public class BigDecimalWrap {

    private BigDecimal rawBigDecimal;

    public BigDecimalWrap(BigDecimal bigDecimal) {
        this.rawBigDecimal = bigDecimal;
    }

    public BigDecimalWrap(BigInteger bigInteger) {
        this.rawBigDecimal = new BigDecimal(bigInteger);
    }

    public BigDecimalWrap(Integer integer) {
        this.rawBigDecimal = new BigDecimal(integer);
    }

    public BigDecimalWrap(Double d) {
        this.rawBigDecimal = new BigDecimal(d);
    }

    public BigDecimal add(Object x) {
        if (x instanceof BigDecimal) {
            return rawBigDecimal.add((BigDecimal) x);
        }
        return rawBigDecimal.add(new BigDecimal(x.toString()));
    }

    public BigDecimal subtract(Object x) {
        if (x instanceof BigDecimal) {
            return rawBigDecimal.subtract((BigDecimal) x);
        }
        return rawBigDecimal.subtract(new BigDecimal(x.toString()));
    }

    public BigDecimal multiply(Object x) {
        if (x instanceof BigDecimal) {
            return rawBigDecimal.multiply((BigDecimal) x);
        }
        return rawBigDecimal.multiply(new BigDecimal(x.toString()));
    }

    public BigDecimal divide(Object x) {
        if (x instanceof BigDecimal) {
            return rawBigDecimal.divide((BigDecimal) x);
        }
        return rawBigDecimal.divide(new BigDecimal(x.toString()));
    }

    /**
     * equal
     * @param x
     * @return
     */
    public boolean eq(Object x) {
        BigDecimal bigDecimal = MathNativeFunction.toBigDecimal(x);
        return rawBigDecimal.compareTo(bigDecimal) == 0;
    }

    /**
     * greater than
     * @param x
     * @return
     */
    public boolean gt(Object x) {
        BigDecimal bigDecimal = MathNativeFunction.toBigDecimal(x);
        return rawBigDecimal.compareTo(bigDecimal) > 0;
    }

    /**
     * equal or greater than
     * @param x
     * @return
     */
    public boolean gte(Object x) {
        BigDecimal bigDecimal = MathNativeFunction.toBigDecimal(x);
        return rawBigDecimal.compareTo(bigDecimal) >= 0;
    }

    /**
     * less than
     * @param x
     * @return
     */
    public boolean lt(Object x) {
        BigDecimal bigDecimal = MathNativeFunction.toBigDecimal(x);
        return rawBigDecimal.compareTo(bigDecimal) < 0;
    }

    /**
     * equal or less than
     * @param x
     * @return
     */
    public boolean lte(Object x) {
        BigDecimal bigDecimal = MathNativeFunction.toBigDecimal(x);
        return rawBigDecimal.compareTo(bigDecimal) <= 0;
    }

    public BigDecimal getRawBigDecimal() {
        return rawBigDecimal;
    }

    @Override
    public String toString() {
        return rawBigDecimal.toString();
    }
}
