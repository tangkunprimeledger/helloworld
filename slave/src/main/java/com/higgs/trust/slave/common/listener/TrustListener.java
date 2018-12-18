package com.higgs.trust.slave.common.listener;

import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.slave.model.bo.BlockHeader;

/**
 * @author duhongming
 * @date 2018/12/17
 */
public interface TrustListener {

    void onBlock(BlockHeader header);

    void onTransactionExecuted(TransactionResultInfo resultInfo);
}
