package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
public interface SlaveCallbackHandler {
    /**
     * on tx validated
     * @param txId
     */
    void onValidated(String txId);

    /**
     * on tx persisted
     * @param respData
     */
    void onPersisted(RespData<CoreTransaction> respData);

    /**
     * when the cluster persisted of tx
     *
     * @param respData
     */
    void onClusterPersisted(RespData<CoreTransaction> respData);
}
