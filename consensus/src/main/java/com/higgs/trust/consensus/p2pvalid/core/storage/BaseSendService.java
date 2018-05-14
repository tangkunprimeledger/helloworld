package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseSendService {

    @Autowired protected ClusterInfo clusterInfo;

    @Autowired protected P2pConsensusClient p2pConsensusClient;

    public abstract <T extends ResponseCommand> T send(ValidCommand<?> validCommand);

}
