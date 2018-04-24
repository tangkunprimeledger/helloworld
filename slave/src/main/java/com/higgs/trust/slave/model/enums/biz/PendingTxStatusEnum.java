package com.higgs.trust.slave.model.enums.biz;

/**
 * @author tangfashuang
 * @date 2018/04/09 17:45
 * @desc pending transaction status
 */
public enum PendingTxStatusEnum {
    INIT("INIT", "master received transaction"), PACKAGED("PACKAGED", "master put transaction to package");

    PendingTxStatusEnum(String code, String desc) {
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

    public static PendingTxStatusEnum getByCode(String code) {
        for (PendingTxStatusEnum enumeration : values()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }
}
