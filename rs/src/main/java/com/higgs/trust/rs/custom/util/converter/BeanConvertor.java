package com.higgs.trust.rs.custom.util.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyu on 17/12/18.
 * javabean 属性copy
 */
public class BeanConvertor {
    private final static Logger LOG = Logger.getLogger(BeanConvertor.class);

    /**
     * bean之间转换,返回新对象
     *
     * @param from
     *          原对象
     * @param toClazz
     *          目标对象
     * @param <T>
     * @return
     */
    public static <T> T convertBean(Object from,Class<T> toClazz){
        if(from == null){
            return null;
        }
        try {
            T dest = toClazz.newInstance();
            BeanUtils.copyProperties(from, dest);
            return dest;
        }catch(Exception e){
            LOG.error("[convertBean]has error",e);
        }
        return null;
    }
    /**
     * list bean 转换,返回含有新对象的list
     * 调用方需判断结果是否为空
     *
     * @param from
     *          原list
     * @param toClazz
     *          目标list中的对象class
     * @param <T>
     * @param <O>
     * @return
     */
    public static <T,O> List<T> convertList(List<O> from, Class<T> toClazz){
        if(CollectionUtils.isEmpty(from)){
            return null;
        }
        List<T> list = new ArrayList<>(from.size());
        for(O o : from){
            try {
                T dest = toClazz.newInstance();
                BeanUtils.copyProperties(o,dest);
                list.add(dest);
            } catch (Exception e) {
                LOG.error("[convertList]has error",e);
                return null;
            }
        }
        return list;
    }
}
