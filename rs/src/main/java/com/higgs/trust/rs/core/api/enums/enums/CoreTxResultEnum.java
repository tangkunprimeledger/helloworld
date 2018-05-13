package com.higgs.trust.rs.core.api.enums.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public enum CoreTxResultEnum {
    SUCCESS("SUCCESS","the tx execute success"),
    FAIL("FAIL","the tx execute fail");

    private String code;
    private String desc;

    CoreTxResultEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static CoreTxResultEnum formCode(String code){
        for(CoreTxResultEnum coreTxStatusEnum : values()){
            if(StringUtils.equals(code,coreTxStatusEnum.getCode())){
                return coreTxStatusEnum;
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
