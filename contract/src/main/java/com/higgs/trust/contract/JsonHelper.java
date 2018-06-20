package com.higgs.trust.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author duhongming
 * @date 2018/6/13
 */
public class JsonHelper {
    public final static int JSON_GENERATE_FEATURES;

    static {
        int features = 0;
        features = features | SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.SortField.getMask();
        features |= SerializerFeature.MapSortField.getMask();
        JSON_GENERATE_FEATURES = features;
    }

    public static String serialize(Object obj) {
        return JSON.toJSONString(obj, JSON_GENERATE_FEATURES);
    }

    public static Object parse(String json) {
        Object obj = JSON.parse(json);
        return obj;
    }

    public static <T> T  parseObject(String json, Class<T> clazz) {
        T obj = JSON.parseObject(json, clazz);
        return obj;
    }

    public static Object clone(Object obj) {
        String json = serialize(obj);
        Object cloneObj = parse(json);
        return cloneObj;
    }

    public static <T> T clone(Object obj, Class<T> clazz) {
        String json = serialize(obj);
        T cloneObj = JSON.parseObject(json, clazz);
        return cloneObj;
    }
}
