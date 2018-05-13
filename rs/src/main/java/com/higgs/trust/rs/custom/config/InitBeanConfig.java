package com.higgs.trust.rs.custom.config;

import com.higgs.trust.slave.asynctosync.HashBlockingMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @author: pengdi
 **/
@Configuration public class InitBeanConfig {
    @Bean public HashBlockingMap rsResultMap() {
        return new HashBlockingMap<>();
    }
}
