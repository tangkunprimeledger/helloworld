package com.higgs.trust.slave.core.service.version;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.model.bo.context.TransactionData;

/**
 * @author WangQuanzhou
 * @desc transaction processor
 * @date 2018/3/28 18:00
 */
public interface TransactionProcessor {
    void process(TransactionData transactionData, TxProcessTypeEnum processTypeEnum);
}
