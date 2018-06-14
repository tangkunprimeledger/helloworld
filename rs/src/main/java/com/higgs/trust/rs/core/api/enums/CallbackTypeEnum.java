package com.higgs.trust.rs.core.api.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
public enum CallbackTypeEnum {
    ALL("ALL", "callback all"), SELF("SELF", "callback self");

    private String code;
    private String desc;

    CallbackTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CallbackTypeEnum fromCode(String code) {
        for (CallbackTypeEnum typeEnum : values()) {
            if (StringUtils.equals(code, typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
