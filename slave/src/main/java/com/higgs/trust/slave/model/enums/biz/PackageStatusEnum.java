package com.higgs.trust.slave.model.enums.biz;

/**
 * @author tangfashuang
 * @date 2018/04/09 17:48
 * @desc package status
 */
public enum PackageStatusEnum {
    //@formatter:off
    INIT("INIT", "init"),
    SUBMIT_CONSENSUS_SUCCESS("SUBMIT_CONSENSUS_SUCCESS","submit consensus success"),
    RECEIVED("RECEIVED", "package received from consensus"),
    VALIDATING("VALIDATING", "package do validating"),
    VALIDATED("VALIDATED", "package complete validate"),
    PERSISTING("PERSISTING", "package do persisting"),
    PERSISTED("PERSISTED", "package complete persist"),
    WAIT_VALIDATE_CONSENSUS("WAIT_VALIDATE_CONSENSUS","self validating end"),
    WAIT_PERSIST_CONSENSUS("WAIT_PERSIST_CONSENSUS","self persisting end"),
    FAILOVER("FAILOVER","failover package");
    //@formatter:on

    PackageStatusEnum(String code, String desc) {
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

    public static PackageStatusEnum getByCode(String code) {
        for (PackageStatusEnum enumeration : values()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }
}
