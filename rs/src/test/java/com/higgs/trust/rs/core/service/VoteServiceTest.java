package com.higgs.trust.rs.core.service;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.core.api.VoteService;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-08
 */
public class VoteServiceTest extends IntegrateBaseTest {
    @Autowired VoteService voteService;

    @Test
    public void testRequestVoting() {
        CoreTxBO coreTxBO = new CoreTxBO();
        List<String> voters = new ArrayList<>();
        VotePatternEnum votePattern = VotePatternEnum.ASYNC;
        List<VoteReceipt> receipts = voteService.requestVoting(coreTxBO, voters, votePattern);
        System.out.println(receipts);
    }

    @Test
    public void testAcceptVoting() {
        VotingRequest votingRequest =
            new VotingRequest("TEST-A", new CoreTransaction(), VotePatternEnum.SYNC.getCode());
        VoteReceipt voteReceipt = voteService.acceptVoting(votingRequest);
        System.out.println(voteReceipt);
    }

    @Test
    public void testReceiptVote() {
        String txId = "";
        boolean agree = false;
        voteService.receiptVote(txId,agree);
    }

    @Test
    public void testAcceptReceipt() {
        ReceiptRequest receiptRequest = new ReceiptRequest();
        receiptRequest.setTxId("xxx");
        receiptRequest.setVoter("voter-A");
        receiptRequest.setSign("sign");
        receiptRequest.setVoteResult(VoteResultEnum.AGREE.getCode());
        RespData<String> respData = voteService.acceptReceipt(receiptRequest);
        System.out.println(respData);
    }
}
