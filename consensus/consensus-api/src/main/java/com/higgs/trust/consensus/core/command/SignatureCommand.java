/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core.command;

/**
 * @author suimi
 * @date 2018/6/1
 */
public interface SignatureCommand{

    String getNodeName();

    String getSignValue();

    String getSignature();
}
