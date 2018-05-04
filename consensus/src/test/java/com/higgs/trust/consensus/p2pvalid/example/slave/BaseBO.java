package com.higgs.trust.consensus.p2pvalid.example.slave;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import java.io.Serializable;

/**
 * the base object create all BO object
 *
 * @author baizhengwen
 * @create 2017-03-07 21:01
 */
public abstract class BaseBO implements Serializable {

    @Override public String toString() {
        try {
            // ref:https://stackoverflow.com/a/8200915，integrate MULTI_LINE_STYLE with SHORT_PREFIX_STYLE
            StandardToStringStyle style = new StandardToStringStyle();
            style.setContentStart("[");
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