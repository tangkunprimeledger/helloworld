package com.higgs.trust.slave.api.enums.account;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description the account balance change direction
 * @date 2018-03-28
 */
public enum ChangeDirectionEnum {
    INCREASE("INCREASE", "increase in balance"), DECREASE("DECREASE", "decrease in balance");

    private String code;
    private String desc;

    ChangeDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ChangeDirectionEnum getBycode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (ChangeDirectionEnum item : values()) {
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
