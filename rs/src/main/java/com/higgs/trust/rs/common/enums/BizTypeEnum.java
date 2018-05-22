package com.higgs.trust.rs.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
//TODO 使用枚举不是很好。在需要添加业务类型时。所有rs-core 都得修改
public enum BizTypeEnum {
    STORAGE("STORAGE","STORAGE DATAS"),
    ISSUE_UTXO("ISSUE_UTXO","ISSUE UTXO"),
    TRANSFER_UTXO("TRANSFER_UTXO","TRANSFER  UTXO"),
    NOP("NOP", "NO OPERATION"),
    CREATING_CONTRACT("CREATING_CONTRACT", "CREATING CONTRACT"),
    INVOKE_CONTRACT("INVOKE_CONTRACT", "INVOKE CONTRACT"),
    REGISTER_RS("REGISTER_RS", "CREATING RS"),
    REGISTER_POLICY("REGISTER_POLICY", "CREATING POLICY"),
    ;


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
