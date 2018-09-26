package com.higgs.trust.network.eventbus;

import java.lang.reflect.Method;

/**
 * @author duhongming
 * @date 2018/8/16
 */
public class Subscriber {
    private final Object subscribe;
    private final Method method;
    private boolean disable;

    public Subscriber(Object subscribe, Method method) {
        this.subscribe = subscribe;
        this.method = method;
    }

    public Object getSubscribeObject() {
        return subscribe;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean b) {
    }
}
