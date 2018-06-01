/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.command;

/**
 * @author suimi
 * @date 2018/6/1
 */
public abstract class SignatureCommand<T> extends AbstractConsensusCommand<T> {
    public SignatureCommand(T value) {
        super(value);
    }

    public abstract String getNodeName();

    public abstract String getSignValue();

    public abstract String getSignature();
}
