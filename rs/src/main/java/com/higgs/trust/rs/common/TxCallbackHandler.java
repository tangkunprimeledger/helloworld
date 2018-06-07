package com.higgs.trust.rs.common;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface TxCallbackHandler {
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
}
