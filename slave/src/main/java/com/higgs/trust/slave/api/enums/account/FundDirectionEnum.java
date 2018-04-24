package com.higgs.trust.slave.api.enums.account;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description fund direction DEBIT or CREDIT
 * @date 2018-03-27
 */
public enum FundDirectionEnum {
    DEBIT("DEBIT", "debit balance"), CREDIT("CREDIT", "credit balance");

    private String code;
    private String desc;

    FundDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FundDirectionEnum getBycode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (FundDirectionEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
