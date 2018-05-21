package com.higgs.trust.consensus.bftsmart.started.config;

import com.higgs.trust.consensus.bftsmart.started.server.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnExpression("'${bftSmart.systemConfigs.myId}' != ''")
public class SmartServerConfig {

    @Value("${bftSmart.systemConfigs.myId}")
    private String myId;

    @Bean("server")
    @DependsOn("smartConfig")
    public Server getServer() {
        System.out.println(myId);
        if (!StringUtils.isEmpty(myId)) {
            return new Server(Integer.valueOf(myId));
        } else {
            throw new RuntimeException("The myId is not found");
        }
    }
}
