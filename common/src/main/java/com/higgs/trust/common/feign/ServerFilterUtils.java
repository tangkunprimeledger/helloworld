/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.common.feign;

import com.netflix.loadbalancer.Server;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author suimi
 * @date 2018/4/25
 */
class ServerFilterUtils {

    static List<Server> chooseServers(List<Server> servers, Object key) {
        if (key == null) {
            return servers;
        }
        if (key instanceof HttpHeaders) {
            HttpHeaders headers = (HttpHeaders)key;
            List<Server> matchedServers = new ArrayList<>();
            if (headers.containsKey(FeignRibbonConstants.NODE_NAME)) {
                String nodeName = headers.getFirst(FeignRibbonConstants.NODE_NAME);
                if (StringUtils.isNotBlank(nodeName)) {
                    servers.forEach(server -> {
                        String appName = server.getMetaInfo().getAppName();
                        if (appName.equalsIgnoreCase(nodeName)) {
                            matchedServers.add(server);
                        }
                    });
                    return matchedServers;
                }
            }
            if (headers.containsKey(FeignRibbonConstants.NODE_NAME_REG)) {
                String reg = headers.getFirst(FeignRibbonConstants.NODE_NAME_REG);
                if (StringUtils.isNotBlank(reg)) {
                    servers.forEach(server -> {
                        String appName = server.getMetaInfo().getAppName().toUpperCase(Locale.ROOT);
                        if (appName.matches(reg)) {
                            matchedServers.add(server);
                        }
                    });
                    return matchedServers;
                }
            }
        }
        return servers;
    }
}
