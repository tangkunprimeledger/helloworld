package com.higgs.trust.slave.core.service.action;

import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;

/**
 * @Description:
 * @author: pengdi
 **/
public interface ActionHandler {
    /**
     * params verify
     *
     * @return
     */
    void verifyParams(Action action)throws SlaveException;
    /**
     * the storage for the action
     *
     * @param actionData
     */
    void process(ActionData actionData);

}
