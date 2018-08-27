package com.higgs.trust.slave.model.enums;

/**
 * @author liuyu
 * @description
 * @date 2018-04-12
 */
public enum UsageEnum {
    BIZ("biz"), CONSENSUS("consensus"),;
    private String code;

    UsageEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
