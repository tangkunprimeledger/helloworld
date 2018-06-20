package com.higgs.trust.config.node.listener;

import com.higgs.trust.config.node.NodeStateEnum;

import java.lang.annotation.*;

@Documented @Target({ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface StateChangeListener {

    NodeStateEnum[] value();

    boolean before() default false;
}
