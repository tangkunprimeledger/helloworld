package com.higgs.trust.rs.core.api.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public enum CoreTxStatusEnum {
    INIT("INIT","the init status"),
    WAIT("WAIT","wait submit to slave"),
    VALIDATED("VALIDATED","slave validated"),
    PERSISTED("PERSISTED","slave persisted"),
    END("END","the end status");

    private String code;
    private String desc;

    CoreTxStatusEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static CoreTxStatusEnum formCode(String code){
        for(CoreTxStatusEnum coreTxStatusEnum : values()){
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
