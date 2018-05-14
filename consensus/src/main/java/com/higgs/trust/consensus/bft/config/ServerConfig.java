package com.higgs.trust.consensus.bft.config;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Validated
@Configuration
@ConditionalOnExpression("!'${copycat.server.cluster:}'.isEmpty()")
public class ServerConfig
        implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(ServerConfig.class);

    private ApplicationContext applicationContext;

    @NotNull(message = "address can not be null")
    @Value("${copycat.server.address}")
    private String address;

    @NotNull(message = "cluster can not be null")
    @Value("${copycat.server.cluster}")
    private String cluster;

    @NotNull(message = "stateMachineClass can not be null")
    @Value("${copycat.server.stateMachineClass}")
    private String
            stateMachineClass;

    @Value("${copycat.server.nettyThreadNum:10}")
    private Integer nettyThreadNum;

    @Value("${copycat.server.logDir:copycat/logs}")
    private String logDir;

    @Value("${copycat.server.minorCompactionInterval:12000}")
    private Long minorCompactionInterval;

    @Value("${copycat.server.entryBufferSize:200}")
    private Integer entryBufferSize;

    @Value("${copycat.server.compactionThreads:4}")
    private Integer compactionThreads;

    @Value("${copycat.server.maxEntriesPerSegment:20000}")
    private Integer maxEntriesPerSegment;

    @Value("${copycat.server.majorCompactionInterval:60000}")
    private Long majorCompactionInterval;

    @Value("${copycat.server.compactionThreshold:0.01}")
    private Double compactionThreshold;

    @Value("${copycat.server.electionTimeout:2000}")
    private Long electionTimeout;

    @Value("${copycat.server.heartbeatInterval:500}")
    private Long heartbeatInterval;

    @Value("${copycat.server.sessionTimeout:5000}")
    private Long sessionTimeout;

    @Value("${copycat.server.backlog:50}")
    private Integer backlog;

    @Override
    public String toString() {
        return "ServerConfig{" + "address='" + address + '\'' + ", cluster='" + cluster + '\'' + ", stateMachineClass='"
                + stateMachineClass + '\'' + ", nettyThreadNum=" + nettyThreadNum + ", logDir='" + logDir + '\''
                + ", minorCompactionInterval=" + minorCompactionInterval + ", entryBufferSize=" + entryBufferSize
                + ", compactionThreads=" + compactionThreads + ", maxEntriesPerSegment=" + maxEntriesPerSegment
                + ", majorCompactionInterval=" + majorCompactionInterval + ", compactionThreshold=" + compactionThreshold
                + '}';
    }

    @PostConstruct
    public void start() throws ClassNotFoundException {
        log.info("copycat server config : {}", toString());
        Address addressT = new Address(address);
        CopycatServer.Builder builder = CopycatServer.builder(addressT);
        Class<? extends StateMachine> clazz = (Class<? extends StateMachine>) Class.forName(stateMachineClass);
        StateMachine stateMachine = applicationContext.getBean(clazz);
        builder.withStateMachine(() -> {
            try {
                return stateMachine;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.withTransport(NettyTransport.builder()
                .withAcceptBacklog(Math.min(backlog,500))
                .withThreads(nettyThreadNum).build());
        builder.withElectionTimeout(Duration.ofMillis(electionTimeout))
                .withHeartbeatInterval(Duration.ofMillis(heartbeatInterval))
                .withSessionTimeout(Duration.ofMillis(sessionTimeout));

        Storage storage = Storage.builder().withStorageLevel(StorageLevel.DISK).withDirectory(logDir)
                .withMinorCompactionInterval(Duration.ofMillis(minorCompactionInterval))
                .withEntryBufferSize(entryBufferSize).withCompactionThreads(compactionThreads)
                .withMaxEntriesPerSegment(maxEntriesPerSegment)
                .withMajorCompactionInterval(Duration.ofMillis(majorCompactionInterval))
                .withCompactionThreshold(compactionThreshold).build();

        builder.withStorage(storage);
        CopycatServer server = builder.build();
        List<String> clusterList = Arrays.asList(cluster.split(","));
        List<Address> clusterAddress = new ArrayList<>();
        for (String addressStr : clusterList) {
            clusterAddress.add(new Address(StringUtils.trim(addressStr)));
        }
        log.info("copycat cluster start ...");
        server.bootstrap(clusterAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
