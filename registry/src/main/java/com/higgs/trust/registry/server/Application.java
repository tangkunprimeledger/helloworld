package com.higgs.trust.registry.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 启动类
 *
 * @author baizhengwen
 * @date 2018-01-16
 */
@EnableEurekaServer @SpringBootApplication public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

