package com.higgs.trust.rs.custom.util;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author baizhengwen
 * @create 2017-07-31 18:00
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String content = IOUtils.toString(resource.getResource().getURI(), Charsets.UTF_8.name());
        return new MapPropertySource(name, nest(JSON.parseObject(content)));
    }

    private Map<String, Object> nest(final Map<String, Object> data) {
        Map<String, Object> result = Maps.newHashMap();
        nest(null, data, result);
        return result;
    }

    private void nest(String prefix, final Map<String, Object> data, final Map<String, Object> result) {
        if (StringUtils.isNotBlank(prefix)) {
            prefix += '.';
        } else {
            prefix = StringUtils.EMPTY;
        }

        Object value = null;
        for (String key : data.keySet()) {
            value = data.get(key);
            if (value instanceof Map) {
                nest(prefix + key, (Map<String, Object>) value, result);
            } else {
                result.put(prefix + key, value);
            }
        }
    }

}
