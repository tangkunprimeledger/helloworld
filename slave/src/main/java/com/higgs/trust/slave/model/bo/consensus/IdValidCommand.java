/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author suimi
 * @date 2018/4/24
 */
@Getter @Setter @NoArgsConstructor public abstract class IdValidCommand<T extends Serializable> extends ValidCommand<T> {

    private static final long serialVersionUID = -5384005328220736154L;

     private String requestId;

    public IdValidCommand(String requestId, T t) {
        super(t);
        this.requestId = requestId;
    }

}
