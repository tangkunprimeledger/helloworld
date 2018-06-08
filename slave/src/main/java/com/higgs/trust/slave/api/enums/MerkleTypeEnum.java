package com.higgs.trust.slave.api.enums;

/**
 * @author WangQuanzhou
 * @desc merkle tree type enum
 * @date 2018/3/27 11:55
 */
public enum MerkleTypeEnum {
    ACCOUNT("ACCOUNT", "account type"), CONTRACT("CONTRACT", "contract type"),
    TX("TX", "transaction type"), TX_RECEIEPT("TX_RECEIEPT", "txReceiept type"),
    POLICY("POLICY", "policy type"), RS("RS", "rs type"), CA("CA", "CA type"),;

    String code;
    String desc;

    MerkleTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MerkleTypeEnum getBizTypeEnumBycode(String code) {
        for (MerkleTypeEnum versionEnum : MerkleTypeEnum.values()) {
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
