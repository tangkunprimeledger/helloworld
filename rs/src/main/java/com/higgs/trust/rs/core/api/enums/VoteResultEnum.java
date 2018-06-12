package com.higgs.trust.rs.core.api.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
public enum VoteResultEnum {
    INIT("INIT", "INIT status for vote request"),
    AGREE("AGREE", "AGREE status for vote"),
    DISAGREE("DISAGREE", "DISAGREE status for vote");

    private String code;
    private String desc;

    VoteResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static VoteResultEnum fromCode(String code) {
        for (VoteResultEnum patternEnum : values()) {
            if (StringUtils.equals(code, patternEnum.getCode())) {
                return patternEnum;
            }
        }
        return null;
    }
}
