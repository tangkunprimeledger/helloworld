/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.core.integration;

import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient("${higgs.trust.prefix}")
public interface ServiceProviderClient {
    /**
     * voting transaction
     *
     * @param nodeName
     * @param votingRequest
     * @return
     */
    @RequestMapping(value = "/voting", method = RequestMethod.POST)
    VoteReceipt voting(String nodeName, @RequestBody VotingRequest votingRequest);

    /**
     * receipt vote
     *
     * @param nodeName
     * @param receiptRequest
     * @return
     */
    @RequestMapping(value = "/receipting", method = RequestMethod.POST)
    RespData<String> receipting(String nodeName, @RequestBody ReceiptRequest receiptRequest);

}
