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
     * submit transaction from custom rs
     *
     * @param bizType
     * @param coreTx
     */
    void submitTx(BizTypeEnum bizType,CoreTransaction coreTx);

    /**
     * 同步等待
     * @param txId
     * @param forEnd
     * @return
     */
    RespData syncWait(String txId, boolean forEnd);

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
