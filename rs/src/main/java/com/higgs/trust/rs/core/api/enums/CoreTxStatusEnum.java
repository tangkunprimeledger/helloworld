package com.higgs.trust.rs.core.api.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public enum CoreTxStatusEnum {
    INIT("01","INIT","the init status"),
    NEED_VOTE("02","NEED_VOTE","need vote by async"),
    WAIT("03","WAIT","wait submit to slave")
    ;

    private String index;
    private String code;
    private String desc;

    CoreTxStatusEnum(String index, String code,String desc){
        this.index = index;
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

    public static CoreTxStatusEnum formIndex(String index){
        for(CoreTxStatusEnum coreTxStatusEnum : values()){
            if(StringUtils.equals(index,coreTxStatusEnum.getIndex())){
                return coreTxStatusEnum;
            }
        }
        return null;
    }

    public static List<String> getIndexList(String index) {
        List<String> indexList = new ArrayList<>();
        if (!StringUtils.isEmpty(index)) {
            for (CoreTxStatusEnum enumeration : values()) {
                if (enumeration.getIndex().compareTo(index) > 0) {
                    indexList.add(enumeration.getIndex());
                }
            }
        } else {
            for (CoreTxStatusEnum enumeration : values()) {
                indexList.add(enumeration.getIndex());
            }
        }
        return indexList;
    }


    public String getCode() {
        return code;
    }
    public String getDesc() {
        return desc;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
