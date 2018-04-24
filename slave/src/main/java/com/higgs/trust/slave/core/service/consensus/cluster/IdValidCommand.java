/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;

/**
 * @author suimi
 * @date 2018/4/24
 */
public abstract class IdValidCommand<T> extends ValidCommand<T> {

    @Getter private String requestId;

    public IdValidCommand(String requestId, T t) {
        super(t);
        this.requestId = requestId;
    }

}
