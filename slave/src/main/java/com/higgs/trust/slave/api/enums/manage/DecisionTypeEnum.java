package com.higgs.trust.slave.api.enums.manage;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
public enum DecisionTypeEnum {
    FULL_VOTE("FULL_VOTE","require all vote"),ONE_VOTE("ONE_VOTE","just one vote")
    ;
    private String code;
    private String desc;

    DecisionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DecisionTypeEnum getBycode(String code) {
        for (DecisionTypeEnum decisionTypeEnum : DecisionTypeEnum.values()) {
            if (decisionTypeEnum.getCode().equals(code)) {
                return decisionTypeEnum;
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
