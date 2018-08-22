package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j public abstract class BaseReceiveService {

    @Autowired protected ValidConsensus validConsensus;

    @Autowired protected ClusterInfo clusterInfo;

    @Autowired private Crypto crypto;

    public ValidResponseWrap<? extends ResponseCommand> receive(ValidCommandWrap validCommandWrap) {
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();
        clusterInfo.refreshIfNeed();
        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNode());

        log.debug("[BaseReceiveService] user={}", validCommandWrap.getFromNode());

        if (!crypto.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(),
                    validCommandWrap, pubKey));
        }
        return receive(validCommandWrap.getValidCommand());
    }

    public abstract ValidResponseWrap<? extends ResponseCommand> receive(ValidCommand<?> validCommand);
}
