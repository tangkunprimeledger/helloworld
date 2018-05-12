package com.higgs.trust.slave.common.util;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author lingchao
 * @date 2018-04-16
 */
@Slf4j public class AmountUtil {
    private static BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999999.9999999999");
    private static BigDecimal MIN_AMOUNT = new BigDecimal("0.0000000000");
    private static BigDecimal MIN_AMOUNT_NEGATIVE = new BigDecimal("-999999999999999999.9999999999");

    /**
     * check amount
     *
     * @param amount
     * @param allowNegative
     * @return
     */
    public static boolean isLegal(String amount,boolean allowNegative) {
        Preconditions.checkNotNull(amount, "amount can not be null");
        boolean isLegal = true;
        BigDecimal validAmount = null;
        try {
            validAmount = new BigDecimal(amount).setScale(10, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            return false;
        }

        int max = validAmount.compareTo(MAX_AMOUNT);
        int min = validAmount.compareTo(allowNegative ? MIN_AMOUNT_NEGATIVE : MIN_AMOUNT);
        if (max == 1 || min == -1 || validAmount.compareTo(MIN_AMOUNT) == 0) {
            isLegal = false;
        }
        return isLegal;
    }
    /**
     * format amount
     *
     * @param amount
     * @return
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            pattern.append("#");
        }
        pattern.append("0.");
        String factor = "0";
        for (int i = 0; i < 10; i++) {
            pattern.append(factor);
        }
        return new DecimalFormat(pattern.toString()).format(amount);
    }

    /**
     * convert string to BigDecimal
     * @param amount
     * @return
     */
    public static BigDecimal convert(String amount) {
        if (StringUtils.isEmpty(amount)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(amount);
        } catch (Exception e) {
            log.error("convert amount has error",e);
        }
        return BigDecimal.ZERO;
    }
}
