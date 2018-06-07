package com.higgs.trust.rs.core.api.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
public enum VotePatternEnum {
    SYNC("SYNC", "vote by sync pattern"), ASYNC("ASYNC", "vote by async pattern");

    private String code;
    private String desc;

    VotePatternEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static VotePatternEnum fromCode(String code) {
        for (VotePatternEnum patternEnum : values()) {
            if (StringUtils.equals(code, patternEnum.getCode())) {
                return patternEnum;
            }
        }
        return null;
    }
}
