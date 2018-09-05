package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.p2pvalid.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j public abstract class BaseReceiveService {

    @Autowired protected ValidConsensus validConsensus;

    @Autowired protected IClusterViewManager viewManager;

    public ValidResponseWrap<? extends ResponseCommand> receive(ValidCommandWrap validCommandWrap) {
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();
        ClusterView view = viewManager.getView(validCommandWrap.getValidCommand().getView());
        if (view == null || StringUtils.isBlank(view.getPubKey(validCommandWrap.getFromNode()))) {
            throw new RuntimeException(String.format("the view not exist or not have pubkey"));
        }

        String pubKey = view.getPubKey(validCommandWrap.getFromNode());
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
