package com.higgs.trust.slave.api.enums;

/**
 * @author WangQuanzhou
 * @desc merkle node operate type enum
 * @date 2018/3/27 11:55
 */
public enum MerkleStatusEnum {
    NO_CHANGE("NO_CHANGE", "no change"), ADD("ADD", "add"),
    MODIFY("MODIFY", "modify"), ;

    String code;
    String desc;

    MerkleStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MerkleStatusEnum getBizTypeEnumBycode(String code) {
        for (MerkleStatusEnum versionEnum : MerkleStatusEnum.values()) {
            if (versionEnum.getCode().equals(code)) {
                return versionEnum;
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
