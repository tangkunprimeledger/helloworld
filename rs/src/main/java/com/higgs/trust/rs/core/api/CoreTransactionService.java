package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;

/**
 * @author liuyu
 * @description transaction service of rs core
 * @date 2018-05-12
 */
public interface CoreTransactionService {
    /**
     * submit transaction from custom rs by synchronous,release when enPersisted callback is finished
     *
     * @param bizType
     * @param coreTx
     * @param signData
     * @return
     */
    RespData syncSubmitTxForPersisted(BizTypeEnum bizType,CoreTransaction coreTx,String signData);

    /**
     * submit transaction from custom rs by synchronous,release when enEnd callback is finished
     *
     * @param bizType
     * @param coreTx
     * @param signData
     * @return
     */
    RespData syncSubmitTxForEnd(BizTypeEnum bizType,CoreTransaction coreTx,String signData);

    /**
     * submit transaction from custom rs
     *
     * @param bizType
     * @param coreTx
     * @param signData
     */
    void submitTx(BizTypeEnum bizType,CoreTransaction coreTx,String signData);
    /**
     * process init data,sign and update tx status to wait,called by scheduler
     *
     * @param txId
     */
    void processInitTx(String txId);

    /**
     * submit to slave for wait status,called by scheduler
     */
    void submitToSlave();
}
