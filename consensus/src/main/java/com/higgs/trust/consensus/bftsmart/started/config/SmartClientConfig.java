package com.higgs.trust.consensus.bftsmart.started.config;

import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.bftsmart.started.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;


@Configuration
@ConditionalOnBean(SmartServerConfig.class)
public class SmartClientConfig {
    @Value("${bftSmart.systemConfigs.myClientId}")
    private String myClientId;


    @Bean(name = "smartClient")
    @DependsOn("server")
    public ConsensusClient consensusClient() {
        if (!StringUtils.isEmpty(myClientId)) {
            return new Client(Integer.valueOf(myClientId));
        }
        throw new RuntimeException("The clientId is not found");
    }
}
