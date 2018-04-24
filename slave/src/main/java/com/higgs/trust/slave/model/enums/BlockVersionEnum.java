package com.higgs.trust.slave.model.enums;

/**
 * @author liuyu
 * @description
 * @date 2018-04-12
 */
public enum BlockVersionEnum {
    V1("v1.0");
    private String code;
    BlockVersionEnum(String code){
        this.code = code;
    }
    public String getCode(){
        return this.code;
    }
}
