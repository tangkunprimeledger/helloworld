package com.higgs.trust.rs.custom.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import java.io.Serializable;

/**
 * BO父类
 *
 * @author lifangao
 * @create 2017-03-10 14:31
 */
public abstract class BaseBO implements Serializable {

    @Override
    public String toString() {
        try {
            // ref:https://stackoverflow.com/a/8200915，同时结合了MULTI_LINE_STYLE 和SHORT_PREFIX_STYLE
            StandardToStringStyle style = new StandardToStringStyle();
//            style.setContentStart(SystemUtils.LINE_SEPARATOR + "[");
            style.setFieldSeparator(" ");
//            style.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
            style.setFieldSeparatorAtStart(true);
            style.setUseShortClassName(true);
            style.setUseIdentityHashCode(false);
//            style.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
            return new ReflectionToStringBuilder(this, style).toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}