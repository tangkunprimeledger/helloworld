package com.higgs.trust.slave.core.service.action;

import com.higgs.trust.slave.model.bo.context.ActionData;

/**
 * @Description:
 * @author: pengdi
 **/
public interface ActionHandler {

    /**
     * the storage for the action
     *
     * @param actionData
     */
    void process(ActionData actionData);

}
