package com.higgs.trust.slave.api.enums.account;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description account status
 * @date 2018-03-27
 */
public enum AccountStateEnum {
    NORMAL("NORMAL", "account is normal"), DESTROY("DESTROY", "account is destoryed");

    private String code;
    private String desc;

    AccountStateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccountStateEnum getBycode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (AccountStateEnum item : values()) {
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
