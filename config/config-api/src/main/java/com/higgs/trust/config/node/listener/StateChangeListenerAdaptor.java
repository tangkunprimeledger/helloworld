/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.node.listener;

import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author suimi
 * @date 2018/6/13
 */
public class StateChangeListenerAdaptor implements Ordered {

    private Object bean;

    private Method method;

    public StateChangeListenerAdaptor(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public void invoke() {
        try {
            method.invoke(bean);
        } catch (Exception e) {
            throw new ConfigException(ConfigError.CONFIG_NODE_STATE_CHANGE_INVOKE_FAILED, e);
        }
    }

    @Override public int getOrder() {
        Order ann = AnnotationUtils.findAnnotation(method, Order.class);
        if (ann != null) {
            return ann.value();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }
}
