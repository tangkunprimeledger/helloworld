package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
public interface SignService {

    /**
     * request sign by other RS
     *
     * @param rsName
     * @param coreTx
     * @return
     */
    String requestSign(String rsName,CoreTransaction coreTx);
    /**
     * sign transaction
     *
     * @param coreTx
     * @return
     */
    RespData<String> signTx(CoreTransaction coreTx);
}
