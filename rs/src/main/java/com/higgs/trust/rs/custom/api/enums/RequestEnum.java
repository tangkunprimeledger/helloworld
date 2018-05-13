package com.higgs.trust.rs.custom.api.enums;

import lombok.Getter;

/**
 * 请求状态枚举
 *
 * @author lingchao
 */
@Getter
public enum RequestEnum {
    PROCESS("PROCESS", "正在处理"),
    DONE("DONE", "处理完成");

    RequestEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;

    }
    private String code;

    private String desc;

}
