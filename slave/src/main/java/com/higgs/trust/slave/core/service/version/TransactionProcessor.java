package com.higgs.trust.slave.core.service.version;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.context.TransactionData;

/**
 * @author WangQuanzhou
 * @desc transaction processor
 * @date 2018/3/28 18:00
 */
public interface TransactionProcessor {
    /**
     * process
     * @param transactionData
     */
    void process(TransactionData transactionData);

    /**
     * get action handler
     *
     * @param typeEnum
     * @return
     */
    ActionHandler getHandlerByType(ActionTypeEnum typeEnum);
}
