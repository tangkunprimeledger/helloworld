package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.impl.AbstractExecuteContextData;
import com.higgs.trust.slave.model.bo.context.ActionData;

import java.util.Map;

/**
 * Standard Execute Context Data
 * @author duhongming
 * @date 2018-04-23
 */
public class StandardExecuteContextData extends AbstractExecuteContextData {

    private static final String ACTION_KEY = "TX_ACTION_DATA_KEY";

    public StandardExecuteContextData() {
        super();
    }

    public StandardExecuteContextData(Map<String, Object> data) {
        super(data);
    }

    public ActionData getAction() {
        return (ActionData) get(ACTION_KEY);
    }
    public StandardExecuteContextData setAction(ActionData action) {
        this.put(ACTION_KEY, action);
        return this;
    }
}
