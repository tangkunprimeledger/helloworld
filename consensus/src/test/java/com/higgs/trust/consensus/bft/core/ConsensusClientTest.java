package com.higgs.trust.consensus.bft.core;

import com.higgs.trust.consensus.bft.example.StringCommand;
import com.higgs.trust.consensus.bft.example.StringStateMachine;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ConsensusClientTest {
    @Autowired
    private ValidConsensus validConsensus;

    List<Address> clusterAddress = new ArrayList<Address>() {{
        add(new Address("127.0.0.1:9800"));
        add(new Address("127.0.0.1:9900"));
    }};

    @Test
    public void testRaftServerAndClient() throws InterruptedException {
        for (Address address : clusterAddress){
            new Thread(()-> startServer(address, clusterAddress)).start();
        }
        CopycatClient copycatClient = startClient(clusterAddress);
        copycatClient.submit(new StringCommand("test string command"));
        Thread.sleep(6000);
    }

    private CopycatClient startClient(List<Address> clusterAddress){
        CopycatClient client = CopycatClient.builder().withTransport(new NettyTransport())
                .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
                .withRecoveryStrategy(RecoveryStrategies.CLOSE)
                .withServerSelectionStrategy(ServerSelectionStrategies.LEADER).build();
        client.connect(clusterAddress).join();
        return client;
    }

    private void startServer(Address address, List<Address> clusterAddress){
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
        CopycatServer server = builder.build();
        log.info("copycat cluster start ...");
        server.bootstrap(clusterAddress);
    }

}