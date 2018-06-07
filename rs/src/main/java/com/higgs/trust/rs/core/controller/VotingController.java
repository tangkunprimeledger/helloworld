/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.VoteService;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyu
 * @date 2018/5/12
 */

@RestController @Slf4j public class VotingController {
    @Autowired private VoteService voteService;

    /**
     * request voting
     *
     * @param votingRequest
     * @return
     */
    @RequestMapping(value = "/voting") VoteReceipt acceptVoting(@RequestBody VotingRequest votingRequest) {
        return voteService.acceptVoting(votingRequest);
    }
    /**
     * request receipting
     *
     * @param receiptRequest
     * @return
     */
    @RequestMapping(value = "/receipting") RespData<String> receiptVote(@RequestBody ReceiptRequest receiptRequest) {
        return voteService.acceptReceipt(receiptRequest);
    }


    /**
     * receipt vote for test
     *
     * @param txId
     * @param agree
     * @return
     */
    @RequestMapping(value = "/receiptVote")void receiptVote(String txId,boolean agree) {
        voteService.receiptVote(txId,agree);
    }
}
