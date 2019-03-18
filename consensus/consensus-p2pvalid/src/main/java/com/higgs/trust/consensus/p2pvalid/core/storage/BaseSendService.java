package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseSendService {

    @Autowired protected P2pConsensusClient p2pConsensusClient;

    @Autowired protected IClusterViewManager viewManager;

    @Autowired protected NodeState nodeState;

    public abstract <T extends ResponseCommand> T send(ValidCommand<?> validCommand);

}
