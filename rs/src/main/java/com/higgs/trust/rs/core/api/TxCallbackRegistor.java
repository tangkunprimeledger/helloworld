package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.callback.TxBatchCallbackHandler;
import com.higgs.trust.rs.core.callback.TxCallbackHandler;
import com.higgs.trust.rs.core.vo.VotingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
@Repository @Slf4j public class TxCallbackRegistor {
    private TxCallbackHandler coreTxCallback;
    private TxBatchCallbackHandler txBatchCallbackHandler;

    public void registCallback(TxCallbackHandler callback) {
        this.coreTxCallback = callback;
    }

    public void registBatchCallback(TxBatchCallbackHandler callback) {
        this.txBatchCallbackHandler = callback;
    }

    public TxCallbackHandler getCoreTxCallback() {
        return coreTxCallback;
    }

    public TxBatchCallbackHandler getCoreTxBatchCallback() {
        return txBatchCallbackHandler;
    }

    /**
     * vote for custom
     *
     * @param votingRequest
     */
    public void onVote(VotingRequest votingRequest) {
        if (coreTxCallback != null) {
            coreTxCallback.onVote(votingRequest);
        } else if (txBatchCallbackHandler != null) {
            txBatchCallbackHandler.onVote(votingRequest);
        } else {
            //TODO:liuyu for press-test
//            log.error("[onVote] callback handler is not register");
//            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
        }
    }
}
