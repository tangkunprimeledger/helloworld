package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.slave.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j public class P2pClusterInfoTest extends BaseTest {
    @Autowired ClusterInfo p2pClusterInfo;

    @Test public void testMyNodeName() throws Exception {
        String myNodeName = p2pClusterInfo.nodeName();
        log.info("my node name : {}", myNodeName);
        Assert.assertNotNull(myNodeName);
    }

    @Test public void testClusterNodeNames() throws Exception {
        List<String> clusterNodeNames = p2pClusterInfo.clusterNodeNames();
        log.info("my cluster node names : {}", clusterNodeNames);
        Assert.assertNotNull(clusterNodeNames);

    }

    @Test public void testPubKey() throws Exception {
        String pubKey = p2pClusterInfo.pubKeyForConsensus("TRUST-node97");
        log.info("get node pub key : {}", pubKey);
        Assert.assertNotNull(pubKey);
    }

    @Test public void testPrivateKey() throws Exception {
        String priKey = p2pClusterInfo.priKeyForConsensus();
        log.info("get node pri key : {}", priKey);
        Assert.assertNotNull(p2pClusterInfo.priKeyForConsensus());
    }

}