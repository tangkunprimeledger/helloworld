package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.vo.ReceiptRequest;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignInfo;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
public interface VoteService {
    /**
     * request voting
     *
     * @param coreTxBO
     * @param voters
     * @param votePattern
     * @return
     */
    List<VoteReceipt> requestVoting(CoreTxBO coreTxBO,List<String> voters, VotePatternEnum votePattern);

    /**
     * accept voting request,return sign info
     *
     * @param votingRequest
     * @return
     */
    VoteReceipt acceptVoting(VotingRequest votingRequest);

    /**
     * receipt vote for ASYNC pattern
     *
     * @param txId
     * @param agree
     */
    void receiptVote(String txId,boolean agree);

    /**
     * accept receipt request
     *
     * @param receiptRequest
     * @return
     */
    RespData<String> acceptReceipt(ReceiptRequest receiptRequest);
    /**
     * get signInfo from voteReceipts
     *
     * @param receipts
     * @return
     */
    List<SignInfo> getSignInfos(List<VoteReceipt> receipts);

    /**
     * get voters from sign info
     *
     * @param signInfos
     * @param rsIds
     * @return
     */
    List<String> getVoters(List<SignInfo> signInfos, List<String> rsIds);

    /**
     * get decision from receipts
     *
     * @param receipts
     * @param decisionType
     * @return
     */
    boolean getDecision(List<VoteReceipt> receipts, DecisionTypeEnum decisionType);
}
