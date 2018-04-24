package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.impl.AbstractExecuteContextData;
import com.higgs.trust.slave.model.bo.action.UTXOAction;

import java.util.Map;

/**
 * the context data container of utxo
 * @author duhongming
 * @date 2018-04-23
 */
public class UTXOExecuteContextData extends AbstractExecuteContextData {

    private final static String ACTION_KEY = "Action";

    public UTXOExecuteContextData() {
        super();
    }

    public UTXOExecuteContextData(Map<String, Object> data) {
        super(data);
    }

    public UTXOAction getAction() {
        return (UTXOAction) get(ACTION_KEY);
    }

    public UTXOExecuteContextData setAction(UTXOAction action) {
        this.put(ACTION_KEY, action);
        return this;
    }
}
