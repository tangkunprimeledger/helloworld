package com.higgs.trust.slave.api.enums;

/**
 * @author WangQuanzhou
 * @desc multi version enum class
 * @date 2018/3/27 11:55
 */
public enum VersionEnum {
    V1("1.0.0", "V1版本"), V2("2.0.0", "V2版本"), V3("3.0.0", "V3版本"), V4("4.0.0", "V4版本"), V5("5.0.0", "V5版本"),;

    String code;
    String desc;

    VersionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VersionEnum getBizTypeEnumBycode(String code) {
        for (VersionEnum versionEnum : VersionEnum.values()) {
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
