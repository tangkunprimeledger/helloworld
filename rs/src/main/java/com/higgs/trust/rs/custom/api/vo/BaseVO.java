package com.higgs.trust.rs.custom.api.vo;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import java.io.Serializable;

/**
 * basevo
 *
 * Created by liuyu on 18/2/9.
 */
public abstract class BaseVO implements Serializable {

    @Override
    public String toString() {
        try {
            StandardToStringStyle style = new StandardToStringStyle();
            style.setFieldSeparator(" ");
            style.setFieldSeparatorAtStart(true);
            style.setUseShortClassName(true);
            style.setUseIdentityHashCode(false);
            return new ReflectionToStringBuilder(this, style).toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}
