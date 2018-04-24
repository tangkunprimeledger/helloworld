package com.higgs.trust.consensus.p2pvalid.example;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;

/**
 * @author cwy
 */
public class StringValidConsensus extends ValidConsensus {

    public StringValidConsensus(ClusterInfo clusterInfo, P2pConsensusClient p2pConsensusClient, String baseDir) {
        super(clusterInfo, p2pConsensusClient, baseDir);
    }

    public void testStringValid(ValidCommit<StringValidCommand> commit) {
        System.out.println("command2 is " + commit.operation().get());
        commit.close();
    }
}
