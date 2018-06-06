package com.higgs.trust.consensus.copycat.config;

import com.higgs.trust.consensus.copycat.adapter.CopycatClientAdapter;
import com.higgs.trust.consensus.copycat.core.CopyCatCommitReplicateComposite;
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
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration @Slf4j @ConditionalOnExpression("!'${copycat.server.cluster:}'.isEmpty()") public class CopycatConfig
    implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired private CopycatProperties copycatProperties;

    @Autowired private StateMachine stateMachine;

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

    @ConditionalOnBean(ConsensusSnapshot.class) @Bean
    public CopycatStateMachine stateMachine(AbstractCommitReplicateComposite commitReplicateComposite,
        ConsensusSnapshot snapshot) {
        return new CopycatStateMachine(commitReplicateComposite, snapshot);
    }

    private void start(CopycatProperties properties, StateMachine stateMachine) {
        log.info("copycat server config : {}", toString());
        Address addressT = new Address(properties.getAddress());
        CopycatServer.Builder builder = CopycatServer.builder(addressT);
        builder.withStateMachine(() -> {
            try {
                return stateMachine;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.withTransport(
            NettyTransport.builder().withAcceptBacklog(Math.min(properties.getBacklog(), 1000)).withRequestTimeout(1500)
                .withThreads(properties.getNettyThreadNum()).build());
        builder.withElectionTimeout(Duration.ofMillis(properties.getElectionTimeout()))
            .withHeartbeatInterval(Duration.ofMillis(properties.getHeartbeatInterval()))
            .withSessionTimeout(Duration.ofMillis(properties.getSessionTimeout()));

        Storage storage = Storage.builder().withStorageLevel(StorageLevel.DISK).withDirectory(properties.getLogDir())
            .withMinorCompactionInterval(Duration.ofMillis(properties.getMinorCompactionInterval()))
            .withEntryBufferSize(properties.getEntryBufferSize())
            .withCompactionThreads(properties.getCompactionThreads())
            .withMaxEntriesPerSegment(properties.getMaxEntriesPerSegment())
            .withMajorCompactionInterval(Duration.ofMillis(properties.getMajorCompactionInterval()))
            .withCompactionThreshold(properties.getCompactionThreshold()).build();

        builder.withStorage(storage);
        CopycatServer server = builder.build();
        List<String> clusterList = Arrays.asList(properties.getCluster().split(","));
        List<Address> clusterAddress = new ArrayList<>();
        for (String addressStr : clusterList) {
            clusterAddress.add(new Address(StringUtils.trim(addressStr)));
        }
        log.info("copycat cluster start ...");
        server.bootstrap(clusterAddress);
    }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
        start(copycatProperties, stateMachine);
    }
}
