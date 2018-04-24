package com.higgs.trust.slave.core.service.action;

import com.higgs.trust.slave.model.bo.context.ActionData;

/**
 * @Description:
 * @author: pengdi
 **/
public interface ActionHandler {
    /**
     * the logic for the action
     *
     * @param actionData
     */
    void validate(ActionData actionData);

    /**
     * the storage for the action
     *
     * @param actionData
     */
    void persist(ActionData actionData);

}
