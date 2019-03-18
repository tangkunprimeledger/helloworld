package com.higgs.trust.rs.core.callback;

import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.model.bo.BlockHeader;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface TxBatchCallbackHandler {
    /**
     * on vote request
     *
     * @param votingRequest
     */
    void onVote(VotingRequest votingRequest);
    /**
     * on slave persisted phase,only current node persisted
     *
     * @param policyId
     * @param txs
     * @param blockHeader
     */
    void onPersisted(String policyId,List<RsCoreTxVO> txs,BlockHeader blockHeader);

    /**
     * on slave end phase,cluster node persisted
     *
     * @param policyId
     * @param txs
     * @param blockHeader
     */
    void onEnd(String policyId,List<RsCoreTxVO> txs,BlockHeader blockHeader);

    /**
     * on fail over call back
     *
     * @param policyId
     * @param txs
     * @param blockHeader
     */
    void onFailover(String policyId,List<RsCoreTxVO> txs,BlockHeader blockHeader);
}
