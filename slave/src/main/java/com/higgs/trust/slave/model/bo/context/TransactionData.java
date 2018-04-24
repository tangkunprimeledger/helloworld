package com.higgs.trust.slave.model.bo.context;

import com.higgs.trust.slave.model.bo.action.Action;

/**
 * @Description:
 * @author: pengdi
 **/
public interface TransactionData extends CommonData {

    /**
     * set the current action in this transaction processing
     *
     * @return
     */
    void setCurrentAction(Action action);

    /**
     *
     * @return
     */
    ActionData getActionData();
}
