package com.higgs.trust.consensus.p2pvalid.api;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import com.higgs.trust.consensus.p2pvalid.core.exchange.SendValidCommandWrap;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${higgs.trust.prefix}") public interface P2pConsensusClient {
    @RequestMapping(value = "/consensus/p2p/receive_command", method = RequestMethod.POST) @ResponseBody
    ValidResponseWrap<? extends ResponseCommand> send(@RequestHeader(FeignRibbonConstants.NODE_NAME) String nodeName,
        @RequestBody ValidCommandWrap validCommandWrap);

    @RequestMapping(value = "/consensus/p2p/receive_command_sync", method = RequestMethod.POST) @ResponseBody
    ValidResponseWrap<? extends ResponseCommand> syncSend(
        @RequestHeader(FeignRibbonConstants.NODE_NAME) String nodeName, @RequestBody ValidCommandWrap validCommandWrap);
}
