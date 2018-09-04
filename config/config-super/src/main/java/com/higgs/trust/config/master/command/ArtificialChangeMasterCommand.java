/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master.command;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author suimi
 * @date 2018/6/5
 */
@ToString(callSuper = true, exclude = {"sign"}) @Getter public class ArtificialChangeMasterCommand
    extends AbstractConsensusCommand<Long> implements SignatureCommand {

    private static final long serialVersionUID = -7339518785030498480L;
    /**
     * the term number
     */
    private long term;

    private long view;

    /**
     * start height
     */
    private long startHeight;

    /**
     * the node name of master
     */
    private String masterName;

    /**
     * signature
     */
    @Setter private String sign;

    public ArtificialChangeMasterCommand(long term, long view, String masterName, long startHeight) {
        super(term);
        this.term = term;
        this.view = view;
        this.masterName = masterName;
        this.startHeight = startHeight;
    }

    @Override public String getNodeName() {
        return masterName;
    }

    @Override public String getSignValue() {
        StringBuffer sb = new StringBuffer();
        sb.append(term).append(masterName).append(startHeight);
        return Hashing.sha256().hashString(sb.toString(), Charsets.UTF_8).toString();
    }

    @Override public String getSignature() {
        return sign;
    }
}
