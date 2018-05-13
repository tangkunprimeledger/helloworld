package com.higgs.trust.rs.custom.api.enums;

/**
 * 请求状态枚举
 *
 * @author lingchao
 */
public enum RequestStatusEnum {
    INIT("初始化", "INIT"),
    PROCESSING("正在处理", "PROCESSING"),
    SUCCESS("处理成功", "SUCCESS"),
    FAILED("处理失败", "FAILED"),
    DUPLICATE("重复请求", "DUPLICATE");

    RequestStatusEnum(String desc, String code) {
        this.desc = desc;
        this.code = code;
    }

    private String desc;

    private String code;


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}
