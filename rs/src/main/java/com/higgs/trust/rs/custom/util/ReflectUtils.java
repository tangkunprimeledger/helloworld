package com.higgs.trust.rs.custom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;


/**
 * 反射工具
 *
 * @author yangjiyun
 * @create 2017 -06-24 12:07
 */
public class ReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 获取对象实例中的属性值
     *
     * @param fieldName
     * @param o
     * @return
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            LOGGER.warn("获取对象{}上的属性{}失败", o, fieldName, e);
            return null;
        }
    }
}
