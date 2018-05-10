package com.higgs.trust.slave.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class SingleNodeConditional implements Condition {
    @Override public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String property = context.getEnvironment().getProperty("consensus.p2p.cluster");
        if (!StringUtils.isBlank(property)) {
            Map<String, String> clusterMap = JSON.parseObject(property, new TypeReference<Map<String, String>>() {
            });
            return clusterMap.size() <= 1;
        }
        return false;
    }
}
