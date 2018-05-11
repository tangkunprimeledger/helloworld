package com.higgs.trust.consensus.p2pvalid.api.controller;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author cwy
 */
@RequestMapping(value = "/consensus/p2p") @RestController @Slf4j public class P2pConsensusController {

    @Autowired private ReceiveService receiveService;

    @Autowired private SyncReceiveService syncReceiveService;

    @RequestMapping(value = "/receive_command", method = RequestMethod.POST) @ResponseBody
    public ValidResponseWrap<? extends ResponseCommand> receiveCommand(@RequestBody ValidCommandWrap validCommandWrap) {
        try {
            receiveService.receive(validCommandWrap);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return ValidResponseWrap.successResponse(null);
    }

    @RequestMapping(value = "/receive_command_sync", method = RequestMethod.POST) @ResponseBody
    public ValidResponseWrap<? extends ResponseCommand> receiveCommandSync(
        @RequestBody ValidCommandWrap validCommandWrap) {
        return syncReceiveService.receive(validCommandWrap);
    }
}
