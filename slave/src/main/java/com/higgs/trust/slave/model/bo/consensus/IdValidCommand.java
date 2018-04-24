/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;

/**
 * @author suimi
 * @date 2018/4/24
 */
public abstract class IdValidCommand<T> extends ValidCommand<T> {

    private static final long serialVersionUID = 7969839656788683599L;

    @Getter private String requestId;

    public IdValidCommand(String requestId, T t) {
        super(t);
        this.requestId = requestId;
    }

}
