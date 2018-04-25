package com.higgs.trust.consensus.bft.core;

import com.higgs.trust.consensus.bft.example.StringStateMachine;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConsensusClientTest {
    @Autowired
    private ValidConsensus validConsensus;

    @Test
    public void testRaft() throws InterruptedException {
        List<Address> clusterAddress = new ArrayList<Address>() {{
            add(new Address("127.0.0.1:8800"));
            add(new Address("127.0.0.1:8900"));
            add(new Address("127.0.0.1:9000"));
        }};
        for(Address address: clusterAddress){
            startServer(address, clusterAddress);
        }
        Thread.sleep(200000);
    }

    private void startServer(Address address, List<Address> clusterAddress){
        Address addressT = new Address(address);
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