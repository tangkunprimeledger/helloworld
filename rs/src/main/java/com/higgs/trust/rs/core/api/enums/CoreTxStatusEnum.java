package com.higgs.trust.rs.core.api.enums;

import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
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
    WAIT("03","WAIT","wait submit to slave"),
    PERSISTED("04","PERSISTED","slave persisted"),
    END("05","END","the end status");

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

    public static List<String> getIndexs(String index) {
        List<String> indexList = new ArrayList<>();
        for (CoreTxStatusEnum enumeration : values()) {
            if (enumeration.getIndex().compareTo(index) > 0) {
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
