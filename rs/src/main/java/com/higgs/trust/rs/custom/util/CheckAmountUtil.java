package com.higgs.trust.rs.custom.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查 amount
 *
 * @author lingchao
 * @create 2017年12月19日11:01
 */
public class CheckAmountUtil {
    private static BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999999.9999999999");
    private static BigDecimal MIN_AMOUNT = new BigDecimal("0.0000000000");

    /**
     * 校验 amount 是否在规定单位内
     * @param amount
     * @return
     */
    public static boolean isLegal(String amount){
        Preconditions.checkNotNull(amount, "amount can not be null");
        boolean isLegal = true;
        BigDecimal validAmount = null;

        try{
            validAmount = new BigDecimal(amount).setScale(10, BigDecimal.ROUND_DOWN);
        } catch (NumberFormatException e){
            return  false;
        }

        int max = validAmount.compareTo(MAX_AMOUNT);
        int min = validAmount.compareTo(MIN_AMOUNT);
        //amout 在 追大和最小之间为合法
        if(max == 1 || min == -1){
            isLegal = false;
        }
        return isLegal;
    }
}
