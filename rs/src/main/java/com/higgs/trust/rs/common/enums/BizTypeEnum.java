package com.higgs.trust.rs.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public enum BizTypeEnum {
    STORAGE("STORAGE","STORAGE DATAS");

    private String code;
    private String desc;

    BizTypeEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }


    public String getDesc() {
        return desc;
    }

    public static BizTypeEnum fromCode(String code){
        for(BizTypeEnum bizTypeEnum : values()){
            if(StringUtils.equals(code,bizTypeEnum.getCode())){
                return bizTypeEnum;
            }
        }
        return null;
    }
}
