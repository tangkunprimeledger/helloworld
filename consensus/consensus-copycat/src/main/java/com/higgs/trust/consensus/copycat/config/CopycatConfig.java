package com.higgs.trust.consensus.copycat.config;

import com.higgs.trust.consensus.copycat.adapter.CopycatClientAdapter;
import com.higgs.trust.consensus.copycat.core.CopyCatCommitReplicateComposite;
import com.higgs.trust.consensus.copycat.core.CopycatSnapshotStateMachine;
import com.higgs.trust.consensus.copycat.core.CopycatStateMachine;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.client.RecoveryStrategies;
import io.atomix.copycat.client.ServerSelectionStrategies;
import io.atomix.copycat.server.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration @Slf4j @ConditionalOnExpression("!'${copycat.server.cluster:}'.isEmpty()") public class CopycatConfig {

    @Autowired private CopycatProperties copycatProperties;

    @Bean public ConsensusClient consensusClient() {
        String server = copycatProperties.getClient();
        log.info("copycat servers : {}", server);

        List<Address> addressList = new ArrayList<>();
        for (String addressStr : Arrays.asList(server.split(","))) {
            addressList.add(new Address(StringUtils.trim(addressStr)));
        }
        CopycatClient client = CopycatClient.builder().withTransport(new NettyTransport())
            .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
            .withRecoveryStrategy(RecoveryStrategies.CLOSE)
            .withServerSelectionStrategy(ServerSelectionStrategies.LEADER).build();
        client.connect(addressList);
        return new CopycatClientAdapter(client);
    }

    @Bean public AbstractCommitReplicateComposite replicateComposite() {
        return new CopyCatCommitReplicateComposite();
    }

    @Bean public StateMachine stateMachine(AbstractCommitReplicateComposite commitReplicateComposite) {
        return new CopycatStateMachine(commitReplicateComposite);
    }

    @ConditionalOnBean(ConsensusSnapshot.class) @Bean @Primary
    public StateMachine stateMachine(AbstractCommitReplicateComposite commitReplicateComposite,
        ConsensusSnapshot snapshot) {
        return new CopycatSnapshotStateMachine(commitReplicateComposite, snapshot);
    }
}
