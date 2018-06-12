package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-13
 */
public interface SlaveCallbackHandler {
    /**
     * on tx validated
     *
     * @param coreTx
     */
    void onValidated(CoreTransaction coreTx);

    /**
     * on tx persisted
     *
     * @param respData
     * @param signInfos
     */
    void onPersisted(RespData<CoreTransaction> respData, List<SignInfo> signInfos);

    /**
     * when the cluster persisted of tx
     *
     * @param respData
     * @param signInfos
     */
    void onClusterPersisted(RespData<CoreTransaction> respData, List<SignInfo> signInfos);
}
