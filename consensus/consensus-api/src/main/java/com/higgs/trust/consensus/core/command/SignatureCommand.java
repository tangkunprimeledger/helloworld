/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.command;

import lombok.ToString;

/**
 * @author suimi
 * @date 2018/6/1
 */
public interface SignatureCommand{

    public abstract String getNodeName();

    public abstract String getSignValue();

    public abstract String getSignature();
}
