package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface SignService {
    /**
     * sign transaction
     *
     * @param coreTx
     * @return
     */
    SignInfo signTx(CoreTransaction coreTx);
}
