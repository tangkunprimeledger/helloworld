package com.higgs.trust.slave.common.enums;

public enum RunModeEnum {
    CLUSTER("cluster", "集群模式启动"), SINGLE("single", "单节点动态加入"),;

    String code;
    String desc;

    RunModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RunModeEnum getBizTypeEnumBycode(String code) {
        for (RunModeEnum versionEnum : RunModeEnum.values()) {
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
