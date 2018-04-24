package com.higgs.trust.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description bean convertor
 * @date 2018-04-10
 */
@Slf4j public class BeanConvertor {

    /**
     * convert bean,return new object
     *
     * @param from
     * @param toClazz
     * @param <T>
     * @return
     */
    public static <T> T convertBean(Object from, Class<T> toClazz) {
        if (from == null) {
            return null;
        }
        try {
            T dest = toClazz.newInstance();
            BeanUtils.copyProperties(from, dest);
            return dest;
        } catch (Exception e) {
            log.error("[convertBean]has error", e);
        }
        return null;
    }

    /**
     * convert list bean,return new list for object
     *
     * @param from
     * @param toClazz
     * @return
     */
    public static <T, O> List<T> convertList(List<O> from, Class<T> toClazz) {
        if (CollectionUtils.isEmpty(from)) {
            return null;
        }
        List<T> list = new ArrayList<>(from.size());
        for (O o : from) {
            try {
                T dest = toClazz.newInstance();
                BeanUtils.copyProperties(o, dest);
                list.add(dest);
            } catch (Exception e) {
                log.error("[convertList]has error", e);
                return null;
            }
        }
        return list;
    }
}
