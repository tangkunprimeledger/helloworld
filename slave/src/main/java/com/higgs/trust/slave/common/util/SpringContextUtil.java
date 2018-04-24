package com.higgs.trust.slave.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author WangQuanzhou
 * @desc acquire spring application context
 * @date 2018/3/27 16:25
 */
@Component public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(String name) throws BeansException {
        return (T)applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) throws BeansException {
        return (T)applicationContext.getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) throws BeansException {
        return (T)applicationContext.getBean(clazz);
    }

}
