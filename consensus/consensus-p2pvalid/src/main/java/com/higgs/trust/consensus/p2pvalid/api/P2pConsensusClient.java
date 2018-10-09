package com.higgs.trust.consensus.p2pvalid.api;

import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "${higgs.trust.prefix}")
public interface P2pConsensusClient {
//    @RequestMapping(value = "/consensus/p2p/receive_command", method = RequestMethod.POST) @ResponseBody
    ValidResponseWrap<ResponseCommand> send(String nodeName,@RequestBody ValidCommandWrap validCommandWrap);

//    @RequestMapping(value = "/consensus/p2p/receive_command_sync", method = RequestMethod.POST) @ResponseBody
    ValidResponseWrap<ResponseCommand> syncSend(String nodeName, @RequestBody ValidCommandWrap validCommandWrap);

//    @RequestMapping(value = "/consensus/p2p/receive_command_sync", method = RequestMethod.POST) @ResponseBody
    ValidResponseWrap<ResponseCommand> syncSendFeign(String nodeNameReg, @RequestBody ValidCommandWrap validCommandWrap);
}
