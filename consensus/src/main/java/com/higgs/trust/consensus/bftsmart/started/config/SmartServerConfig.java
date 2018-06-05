package com.higgs.trust.consensus.bftsmart.started.config;

import com.higgs.trust.consensus.bft.config.ServerConfig;
import com.higgs.trust.consensus.bftsmart.started.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnExpression("'${bftSmart.systemConfigs.myId}' != ''")
public class SmartServerConfig {

    private static final Logger log = LoggerFactory.getLogger(SmartServerConfig.class);

    @Value("${bftSmart.systemConfigs.myId}")
    private String myId;

    @Bean("server")
    @DependsOn("springUtil")
    public Server getServer() {
        log.info("smart server starting,myid={}", myId);
        if (!StringUtils.isEmpty(myId)) {
            return new Server(Integer.valueOf(myId));
        } else {
            log.info("The myId is not found,myid={}", myId);
            throw new RuntimeException("The myId is not found");
        }
    }
}
