package com.higgs.trust.rs.custom.dao.po;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import java.io.Serializable;

/**
 * 所有实体的父类
 *
 * @author baizhengwen
 * @create 2017-03-07 21:01
 */
public abstract class BasePO implements Serializable {

    @Override
    public String toString() {
        try {
            // ref:https://stackoverflow.com/a/8200915，同时结合了MULTI_LINE_STYLE 和SHORT_PREFIX_STYLE
            StandardToStringStyle style = new StandardToStringStyle();
            style.setContentStart("[");
            style.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
            style.setFieldSeparatorAtStart(true);
            style.setUseShortClassName(true);
            style.setUseIdentityHashCode(false);
            style.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
            return new ReflectionToStringBuilder(this, style).toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}