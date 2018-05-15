package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.core.*;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseReceiveService {

    @Autowired protected ValidConsensus validConsensus;

    @Autowired protected ClusterInfo clusterInfo;

    public ValidResponseWrap<? extends ResponseCommand> receive(ValidCommandWrap validCommandWrap) {
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();
        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNode());
        if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(),
                    validCommandWrap, pubKey));
        }
        return receive(validCommandWrap.getValidCommand());
    }

    public abstract ValidResponseWrap<? extends ResponseCommand> receive(ValidCommand<?> validCommand);
}