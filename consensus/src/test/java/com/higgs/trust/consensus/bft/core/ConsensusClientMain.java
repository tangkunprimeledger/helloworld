package com.higgs.trust.consensus.bft.core;

import com.higgs.trust.consensus.bft.example.StringStateMachine;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.client.RecoveryStrategies;
import io.atomix.copycat.client.ServerSelectionStrategies;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConsensusClientMain {

    public static void main(String[] args){
        List<Address> cluster = new ArrayList<>();
        for (String addressStr : args){
            cluster.add(new Address(addressStr));
        }
        startServer(new Address(args[0]), cluster);
    }

    private static CopycatClient startClient(List<Address> clusterAddress){
        CopycatClient client = CopycatClient.builder().withTransport(new NettyTransport())
                .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
                .withRecoveryStrategy(RecoveryStrategies.CLOSE)
                .withServerSelectionStrategy(ServerSelectionStrategies.LEADER).build();
        client.connect(clusterAddress).join();
        return client;
    }

    private static void startServer(Address address, List<Address> clusterAddress){
        Address addressT = address;
        CopycatServer.Builder builder = CopycatServer.builder(addressT);
        builder.withStateMachine(StringStateMachine::new);

        builder.withTransport(NettyTransport.builder().withThreads(4).build());
        Storage storage = Storage.builder().withStorageLevel(StorageLevel.DISK).withDirectory("D:/temp/copycat"+address.port())
                .withMinorCompactionInterval(Duration.ofMillis(1000))
                .withEntryBufferSize(1000).withCompactionThreads(1)
                .withMaxEntriesPerSegment(1000)
                .withMajorCompactionInterval(Duration.ofMillis(2000))
                .withCompactionThreshold(0.1).build();

        builder.withStorage(storage);
        log.info("copycat cluster start ...");
        CopycatServer server = builder.build();
        server.bootstrap(clusterAddress).join();
    }
}
