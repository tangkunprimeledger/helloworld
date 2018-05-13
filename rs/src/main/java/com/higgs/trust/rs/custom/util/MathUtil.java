package com.higgs.trust.rs.custom.util;

import java.math.BigDecimal;

/**
 * 判断资金是否合法
 *
 * @author kongyu
 * @create 2017-12-19
 */
public class MathUtil {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999999.9999999999");

    public static boolean checkAmount(BigDecimal amount) {
        if (BigDecimal.ZERO.compareTo(amount) < 0
                && MAX_AMOUNT.compareTo(amount) >= 0) {
            return true;
        } else {
            return false;
        }
    }
}
