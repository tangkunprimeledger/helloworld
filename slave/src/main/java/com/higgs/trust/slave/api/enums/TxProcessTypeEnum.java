package com.higgs.trust.slave.api.enums;

/**
 * @author liuyu
 * @description transaction processing type
 * @date 2018-04-09
 */
public enum TxProcessTypeEnum {
    VALIDATE("VALIDATE"), PERSIST("PERSIST");

    String code;

    TxProcessTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
