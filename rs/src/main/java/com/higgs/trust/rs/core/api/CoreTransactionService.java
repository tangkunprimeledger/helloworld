package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.vo.CoreTxVO;

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
     * @param coreTxVO
     * @param signData
     */
    void submitTx(BizTypeEnum bizType,CoreTxVO coreTxVO,String signData);

    /**
     * process sign and update tx status to wait,called by scheduler
     *
     * @param txId
     */
    void processSignData(String txId);

    /**
     * submit to slave for wait status,called by scheduler
     */
    void submitToSlave();
}
