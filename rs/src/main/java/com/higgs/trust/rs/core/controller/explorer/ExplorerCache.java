package com.higgs.trust.rs.core.controller.explorer;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liuyu
 * @description
 * @date 2018-07-17
 */
@Component @Slf4j public class ExplorerCache implements InitializingBean {
    @Value("${rs.core.explorer.duration:60}") private Long duration;
    /**
     * 缓存对象
     */
    private Cache<Object, String> CACHE = null;

    @Override public void afterPropertiesSet() throws Exception {
        if (duration == null) {
            duration = 60L;
        }
        CACHE = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(5000)
            .refreshAfterWrite(duration, TimeUnit.SECONDS).build(new CacheLoader<Object, String>() {
                @Override public String load(Object key) throws Exception {
                    return null;
                }
            });
    }

    /**
     * put
     *
     * @param key
     * @param value
     */
    public void put(CacheKey key, Object value) {
        CACHE.put(JSON.toJSONString(key), JSON.toJSONString(value));
    }

    /**
     * get
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(CacheKey key, Class<T> clazz) {
        try {
            String value = null;
            value = CACHE.get(JSON.toJSONString(key), new Callable<String>() {
                @Override public String call() throws Exception {
                    return "-1";
                }
            });
            if (StringUtils.isEmpty(value) || StringUtils.equals("-1", value)) {
                return null;
            }
            return JSON.parseObject(value, clazz);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            log.error("get has error", e);
        } catch (ExecutionException e) {
            log.error("get has error", e);
        } catch (Exception e) {
            log.error("get has error", e);
        }
        return null;
    }

    @AllArgsConstructor @Getter @Setter public static class CacheKey<T> {
        private String type;
        private T keyData;
    }
}
