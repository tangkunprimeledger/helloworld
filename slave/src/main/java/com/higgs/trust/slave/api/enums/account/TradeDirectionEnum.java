package com.higgs.trust.slave.api.enums.account;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description accounting trade direction DEBIT or CREDIT
 * @date 2018-03-27
 */
public enum TradeDirectionEnum {
    DEBIT("DEBIT", "debit"), CREDIT("CREDIT", "credit");

    private String code;
    private String desc;

    TradeDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TradeDirectionEnum getBycode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (TradeDirectionEnum item : values()) {
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
