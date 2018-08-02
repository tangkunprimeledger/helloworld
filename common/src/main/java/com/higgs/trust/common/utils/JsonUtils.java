package com.higgs.trust.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;

/**
 * @author duhongming
 * @date 2018/8/1
 */
public class JsonUtils {
    public final static int JSON_GENERATE_FEATURES;

    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

        int features = 0;
        features = features | SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.MapSortField.getMask();
        JSON_GENERATE_FEATURES = features;
    }

    public static void addDeserializer(Type type, ObjectDeserializer deserializer) {
        ParserConfig.getGlobalInstance().putDeserializer(type, deserializer);
    }

    public static String serialize(Object obj) {
        return JSON.toJSONString(obj, JSON_GENERATE_FEATURES);
    }

    public static String serialize(Object obj, boolean prettyFormat) {
        return prettyFormat
                ? JSON.toJSONString(obj, (JSON_GENERATE_FEATURES | SerializerFeature.PrettyFormat.getMask()))
                : JSON.toJSONString(obj, JSON_GENERATE_FEATURES);
    }

    public static String serializeWithType(Object obj) {
        return JSON.toJSONString(obj, (JSON_GENERATE_FEATURES | SerializerFeature.WriteClassName.getMask()));
    }

    public static String serializeWithType(Object obj, boolean prettyFormat) {
        return prettyFormat
                ? JSON.toJSONString(obj, (JSON_GENERATE_FEATURES | SerializerFeature.WriteClassName.getMask() | SerializerFeature.PrettyFormat.getMask()))
                : JSON.toJSONString(obj, JSON_GENERATE_FEATURES | SerializerFeature.WriteClassName.getMask());
    }

    public static Object parse(String json) {
        Object obj = JSON.parse(json);
        return obj;
    }

    public static JSONObject parseObject(String json) {
        return JSON.parseObject(json);
    }

    public static <T> T  parseObject(String json, Class<T> clazz) {
        T obj = JSON.parseObject(json, clazz);
        return obj;
    }

    public static <T> Object parseArray(String json, Class<T> clazz) {
        Object obj = JSON.parseArray(json, clazz);
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
