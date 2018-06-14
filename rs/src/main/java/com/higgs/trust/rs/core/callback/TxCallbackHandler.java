package com.higgs.trust.rs.core.callback;

import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface TxCallbackHandler {
    /**
     * on vote request
     *
     * @param votingRequest
     */
    void onVote(VotingRequest votingRequest);
    /**
     * on slave persisted phase,only current node persisted
     *
     * @param respData
     */
    void onPersisted(RespData<CoreTransaction> respData);

    /**
     * on slave end phase,cluster node persisted
     *
     * @param respData
     */
    void onEnd(RespData<CoreTransaction> respData);

    /**
     * on fail over call back
     *
     * @param respData
     */
    void onFailover(RespData<CoreTransaction> respData);
}
