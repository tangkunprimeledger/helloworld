package com.higgs.trust.slave.common.enums;

/**  
 * @desc cluster's keyPair generate mode, auto or manual
 * @author WangQuanzhou
 * @date 2018/8/27 19:15
 */  
public enum KeyModeEnum {
    AUTO("auto", "自动生成公私钥模式"), MANUAL("manual", "手动配置公私钥模式"),;

    String code;
    String desc;

    KeyModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static KeyModeEnum getBizTypeEnumBycode(String code) {
        for (KeyModeEnum versionEnum : KeyModeEnum.values()) {
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
