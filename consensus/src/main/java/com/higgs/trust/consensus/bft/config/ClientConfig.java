package com.higgs.trust.consensus.bft.config;

import com.higgs.trust.consensus.bft.adapter.CopycatClientAdapter;
import com.higgs.trust.consensus.bft.core.ConsensusClient;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.client.RecoveryStrategies;
import io.atomix.copycat.client.ServerSelectionStrategies;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnExpression("!'${copycat.client.server:}'.isEmpty()")
@ConditionalOnBean(ServerConfig.class)
public class ClientConfig {
    private static final Logger log = LoggerFactory.getLogger(ClientConfig.class);

    @Value("${copycat.client.server}")
    private String server;

    @Bean
    public ConsensusClient consensusClient() {
        log.info("copycat servers : {}", server);

        List<Address> addressList = new ArrayList<>();
        for (String addressStr : Arrays.asList(server.split(","))) {
            addressList.add(new Address(StringUtils.trim(addressStr)));
        }
        CopycatClient client = CopycatClient.builder().withTransport(new NettyTransport())
                .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
                .withRecoveryStrategy(RecoveryStrategies.CLOSE)
                .withServerSelectionStrategy(ServerSelectionStrategies.LEADER).build();
        client.connect(addressList).join();
        return new CopycatClientAdapter(client);
    }
}
