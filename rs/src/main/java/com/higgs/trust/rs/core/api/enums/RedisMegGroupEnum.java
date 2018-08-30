package com.higgs.trust.rs.core.api.enums;

import lombok.Getter;

/**
 *
 * @author lingchao
 * @description redis meg group  enum
 * @date 2018-08-23
 */
@Getter
public enum RedisMegGroupEnum {
    ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY("ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY","on persisted callback message notify"),
    ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY("ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY","on persisted callback message notify"),
    ;
    private String code;
    private String desc;

    RedisMegGroupEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
