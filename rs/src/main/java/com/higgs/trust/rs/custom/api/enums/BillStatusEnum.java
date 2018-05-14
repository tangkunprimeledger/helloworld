package com.higgs.trust.rs.custom.api.enums;

import lombok.Getter;

/**
 *票据枚举
 *
 * @author lingchao
 */
@Getter
public enum BillStatusEnum {
    PROCESS("PROCESS", "发行中"),
    FAILED("FAILED", "发行失败"),
    UNSPENT("UNSPENT", "未花费"),
    SPENT("SPENT", "已花费");


    BillStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;

    }
    private String code;

    private String desc;
}
