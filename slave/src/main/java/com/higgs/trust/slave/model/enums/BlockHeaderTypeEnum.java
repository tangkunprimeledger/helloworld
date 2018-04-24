package com.higgs.trust.slave.model.enums;

/**
 * @author liuyu
 * @description
 * @date 2018-04-12
 */
public enum BlockHeaderTypeEnum {
    CONSENSUS_VALIDATE_TYPE("CONSENSUS_VALIDATE_TYPE"),
    CONSENSUS_PERSIST_TYPE("CONSENSUS_PERSIST_TYPE"),
    TEMP_TYPE("TEMP_TYPE");
    private String code;
    BlockHeaderTypeEnum(String code){
        this.code = code;
    }
    public String getCode(){
        return this.code;
    }
}
