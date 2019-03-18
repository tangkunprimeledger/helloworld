package com.higgs.trust.slave.model.enums.biz;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/09 17:48
 * @desc package status
 */
public enum PackageStatusEnum {
    //@formatter:off
    RECEIVED("01", "RECEIVED", "package received from consensus"),
    WAIT_PERSIST_CONSENSUS("02", "WAIT_PERSIST_CONSENSUS","self persisting end"),
    PERSISTED("03", "PERSISTED", "package complete persist"),
    FAILOVER("04", "FAILOVER","failover package");
    //@formatter:on

    PackageStatusEnum(String index, String code, String desc) {
        this.index = index;
        this.code = code;
        this.desc = desc;
    }

    private String index;

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static PackageStatusEnum getByCode(String code) {
        for (PackageStatusEnum enumeration : values()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }

    public static PackageStatusEnum getByIndex(String index) {
        for (PackageStatusEnum enumeration : values()) {
            if (enumeration.getIndex().equals(index)) {
                return enumeration;
            }
        }
        return null;
    }

    public static List<String> getIndexs(String index) {
        List<String> indexList = new ArrayList<>();
        if (StringUtils.isEmpty(index)) {
            for (PackageStatusEnum enumeration : values()) {
                indexList.add(enumeration.getIndex());
            }
        } else {
            for (PackageStatusEnum enumeration : values()) {
                if (enumeration.getIndex().compareTo(index) > 0) {
                    indexList.add(enumeration.getIndex());
                }
            }
        }
        return indexList;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
