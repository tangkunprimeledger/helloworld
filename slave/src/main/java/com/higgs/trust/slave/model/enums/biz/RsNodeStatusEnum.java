package com.higgs.trust.slave.model.enums.biz;

public enum RsNodeStatusEnum {
    COMMON("COMMON","rs node register"),
    CANCELED("CANCELED", "rs node canceled");

    RsNodeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static RsNodeStatusEnum getByCode(String code) {
        for (RsNodeStatusEnum enumeration : values()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }
}
