package com.higgs.trust.consensus.p2pvalid.example;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.example.slave.ValidateCommand;

/**
 * @author cwy
 */
public class StringValidConsensus extends ValidConsensus {

    public StringValidConsensus(ClusterInfo clusterInfo, P2pConsensusClient p2pConsensusClient, String baseDir) {
        super(clusterInfo, p2pConsensusClient, baseDir);
    }

    public StringValidConsensus(ClusterInfo clusterInfo, P2pConsensusClient p2pConsensusClient) {
        super(clusterInfo, p2pConsensusClient);
    }

    public void testStringValid(ValidCommit<StringValidCommand> commit) {
        System.out.println("command2 is " + commit.operation().get());
        commit.close();
    }
    public void testValidateCommand(ValidCommit<ValidateCommand> commit) {
        System.out.println("ValidateCommand is " + commit.operation().get());
        commit.close();
    }
}
