package com.higgs.trust.rs.custom.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by liuyu on 18/1/6.
 * 格式化工具
 */
public class FormatUtil {
    /**
     * 隐藏手机号中间四位
     *
     * @param mobile
     * @return
     */
    public static String formatMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return mobile;
        }
        int len = mobile.length();
        if (len == 11) {
            mobile = mobile.substring(0, 3) + "****" + mobile.substring(len - 4);
        } else {
            if (len > 6) {
                mobile = mobile.substring(0, 3) + "***" + mobile.substring(len - 3);
            } else {
                mobile = mobile.substring(0, 2) + "**" + mobile.substring(len - 2);
            }
        }
        return mobile;
    }

    /**
     * 隐藏邮箱@前部分
     *
     * @param email
     * @return
     */
    public static String formatEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int index = email.indexOf("@");
        email = email.substring(0, index - index / 2) + "**" + email.substring(index);
        return email;
    }

    /**
     * 金额格式化
     *
     * @param amount
     * @return
     */
    public static String formatAmount(BigDecimal amount) {
        if(amount == null){
            return "0.0000000000";
        }
        //amount小数补0
        DecimalFormat decimalFormat = new DecimalFormat("#0.0000000000");
        return decimalFormat.format(amount);
    }
    /**
     * 字符串 转 BigDecimal
     *
     * @param str
     * @return
     */
    public static BigDecimal str2BigDecimal(String str) {
        if (StringUtils.isEmpty(str)) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = BigDecimal.ZERO;
        try {
            value = new BigDecimal(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    public static void main(String[] args) {
        String s = formatAmount(new BigDecimal(1.00000600));
        System.out.println(s);
        s = formatMobile("123456");
        System.out.println(s);
    }
}
