/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import lombok.Getter;

/**
 * @author suimi
 * @date 2018/4/24
 */
public abstract class IdConsensusCommand<T> extends AbstractConsensusCommand<T> {

    private static final long serialVersionUID = 8804669091634076151L;

    @Getter private String requestId;

    @Getter private String nodeName;

    public IdConsensusCommand(String requestId, String nodeName, T value) {
        super(value);
        this.requestId = requestId;
        this.nodeName = nodeName;
    }
}
