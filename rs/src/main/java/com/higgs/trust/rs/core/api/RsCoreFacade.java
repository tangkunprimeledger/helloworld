package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description
 * @date 2018-08-28
 */
@Component public class RsCoreFacade {
    @Autowired CoreTransactionService coreTransactionService;

    /**
     * 同步处理交易
     *
     * @param coreTx         要处理的交易对象
     * @param waitForCluster 是否等待集群共识
     *                       共识分为：单机和集群共识，集群共识会稍慢于单机共识
     *                       需根据业务特点选择用哪种
     * @return
     * @RsCoreException 可能会抛出异常，常见幂等异常：RsCoreErrorEnum.RS_CORE_IDEMPOTENT
     */
    public RespData processTx(CoreTransaction coreTx, boolean waitForCluster) throws RsCoreException {
        coreTransactionService.submitTx(coreTx);
        return coreTransactionService.syncWait(coreTx.getTxId(), waitForCluster);
    }

    /**
     * 异步处理交易
     *
     * @param coreTx 要处理的交易对象
     * @return
     * @RsCoreException 可能会抛出异常，常见幂等异常：RsCoreErrorEnum.RS_CORE_IDEMPOTENT
     */
    public void asyncProcessTx(CoreTransaction coreTx) throws RsCoreException {
        coreTransactionService.submitTx(coreTx);
    }

    /**
     * 根据txId查询交易信息及处理结果
     *
     * @param txId
     * @return
     */
    public RsCoreTxVO queryTxById(String txId) {
        return coreTransactionService.queryCoreTx(txId);
    }
}
