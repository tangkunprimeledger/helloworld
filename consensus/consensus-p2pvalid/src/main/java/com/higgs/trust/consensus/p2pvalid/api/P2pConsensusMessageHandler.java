package com.higgs.trust.consensus.p2pvalid.api;

import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.core.service.P2PReceiveService;
import com.higgs.trust.consensus.p2pvalid.core.storage.SyncReceiveService;
import com.higgs.trust.network.NetworkManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author duhongming
 * @date 2018/9/17
 */
@Service
@Slf4j
public class P2pConsensusMessageHandler implements InitializingBean {

    @Autowired
    private NetworkManage networkManage;

    @Autowired private P2PReceiveService receiveService;

    @Autowired private SyncReceiveService syncReceiveService;

    public ValidResponseWrap<? extends ResponseCommand> receiveCommand(ValidCommandWrap validCommandWrap) {
        try {
            receiveService.receive(validCommandWrap);
        } catch (Throwable throwable) {
            log.error("failed process received command", throwable);
            return ValidResponseWrap.failedResponse(throwable.getMessage());
        }
        return ValidResponseWrap.successResponse(null);
    }

    public ValidResponseWrap<? extends ResponseCommand> receiveCommandSync(ValidCommandWrap validCommandWrap) {
        try {
            return syncReceiveService.receive(validCommandWrap);
        } catch (Throwable throwable) {
            log.error("failed process received sync command", throwable);
            return ValidResponseWrap.failedResponse(throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        networkManage.registerHandler("consensus/p2p/receive_command", this::receiveCommand);
        networkManage.registerHandler("consensus/p2p/receive_command_sync", this::receiveCommandSync);
    }
}
