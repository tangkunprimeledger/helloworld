package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.common.utils.CryptoUtil;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j public abstract class BaseReceiveService {

    @Autowired protected ValidConsensus validConsensus;

    @Autowired protected ClusterInfo clusterInfo;

    public ValidResponseWrap<? extends ResponseCommand> receive(ValidCommandWrap validCommandWrap) {
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();
        clusterInfo.refreshIfNeed();
        String pubKey = clusterInfo.pubKeyForConsensus(validCommandWrap.getFromNode());
        if (log.isDebugEnabled()) {
            log.debug("node={},pubKeyForConsensus={}", validCommandWrap.getFromNode(), pubKey);
            log.debug("[BaseReceiveService] user={}", validCommandWrap.getFromNode());
        }

        if (!CryptoUtil.getProtocolCrypto().verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKeyForConsensus %s",
                    validCommandWrap.getFromNode(), validCommandWrap, pubKey));
        }
        log.debug("verify sign success for node={}", validCommandWrap.getFromNode());
        return receive(validCommandWrap.getValidCommand());
    }

    public abstract ValidResponseWrap<? extends ResponseCommand> receive(ValidCommand<?> validCommand);
}
