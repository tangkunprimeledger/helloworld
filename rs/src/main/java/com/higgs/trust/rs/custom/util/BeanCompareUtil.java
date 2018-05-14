package com.higgs.trust.rs.custom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by young001 on 2017/6/30.
 */
public class BeanCompareUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCompareUtil.class);

    /**
     * 检查srcBean和targetBean中compareProperties指定的属性进行比较
     *
     * @param srcBean
     * @param targetBean
     * @param compareProperties
     * @return
     */
    public static boolean compareBeans(Object srcBean, Object targetBean, String[] compareProperties) {
        Boolean compareResult = true;
        try {
            for (String property : compareProperties) {
                Object srcBeanProperty = ReflectUtils.getFieldValueByName(property, srcBean);
                Object targetBeanProperty = ReflectUtils.getFieldValueByName(property, targetBean);
                if (!srcBeanProperty.equals(targetBeanProperty)) {
                    compareResult = false;
                }
            }
        } catch (Exception e) {
            LOGGER.info("比较bean发生异常", e);
            compareResult = false;
        }
        return compareResult;
    }
}
