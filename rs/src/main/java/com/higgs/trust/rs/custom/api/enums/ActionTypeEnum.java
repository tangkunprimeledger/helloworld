package com.higgs.trust.rs.custom.api.enums;

import org.apache.commons.lang.StringUtils;

/**
 * action类型枚举
 *
 * @author baizhengwen
 */
public enum ActionTypeEnum {

    //存证类(001)
    UPDATE("000", "覆盖更新"),
    PRESERVE("999", "不更新");



    String type;
    String msg;
    ActionTypeEnum(String type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static ActionTypeEnum getByType(String type) {
        for (ActionTypeEnum item : values()) {
            if (StringUtils.equals(type, item.getType())) {
                return item;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

}
