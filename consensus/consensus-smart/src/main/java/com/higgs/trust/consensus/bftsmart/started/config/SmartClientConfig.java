package com.higgs.trust.consensus.bftsmart.started.config;

import com.higgs.trust.consensus.bftsmart.started.client.Client;
import com.higgs.trust.consensus.core.ConsensusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;


@Configuration
@ConditionalOnBean(SmartServerConfig.class)
public class SmartClientConfig {

    private static final Logger log = LoggerFactory.getLogger(SmartClientConfig.class);

    @Value("${bftSmart.systemConfigs.myClientId}")
    private String myClientId;


    @Bean(name = "smartClient")
    @DependsOn("server")
    public ConsensusClient consensusClient() {
        log.info("smart client start");
        if (!StringUtils.isEmpty(myClientId)) {
            return new Client(Integer.valueOf(myClientId));
        }
        log.info("The clientId is not found,clientId={}", myClientId);
        throw new RuntimeException("The clientId is not found");
    }
}
