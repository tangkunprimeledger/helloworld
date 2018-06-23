package com.higgs.trust.consensus.config.listener;

import com.higgs.trust.consensus.config.NodeStateEnum;

import java.lang.annotation.*;

@Documented @Target({ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface StateChangeListener {

    NodeStateEnum[] value();

    boolean before() default false;
}
