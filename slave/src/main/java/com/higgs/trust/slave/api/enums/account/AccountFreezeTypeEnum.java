package com.higgs.trust.slave.api.enums.account;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description account freeze type
 * @date 2018-03-30
 */
public enum AccountFreezeTypeEnum {
    FREEZE("FREEZE", "freeze balance"), UNFREEZE("UNFREEZE", "unfreeze balance");

    private String code;
    private String desc;

    AccountFreezeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccountFreezeTypeEnum getBycode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (AccountFreezeTypeEnum item : values()) {
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
