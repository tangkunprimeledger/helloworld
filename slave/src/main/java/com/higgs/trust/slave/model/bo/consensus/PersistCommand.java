package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.slave.model.bo.BlockHeader;

/**
 * @Description:persist p2p command for consensus
 * @author: pengdi
 **/
public class PersistCommand extends ValidCommand<BlockHeader> {
    private static final long serialVersionUID = -1L;

    public PersistCommand(Long seqNum, BlockHeader header) {
        super(header);
    }

    @Override
    public String messageDigest() {
        return get().toString();
    }
}
