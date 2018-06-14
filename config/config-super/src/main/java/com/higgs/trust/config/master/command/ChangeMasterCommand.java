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

import java.util.Comparator;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/5
 */
@ToString(callSuper = true, exclude = {"sign"}) @Getter public class ChangeMasterCommand
    extends AbstractConsensusCommand<Map<String, ChangeMasterVerifyResponse>> implements SignatureCommand {

    private static final long serialVersionUID = -7339518785030498480L;
    /**
     * the term number
     */
    private long term;

    /**
     * the node name of master
     */
    private String masterName;

    /**
     * signature
     */
    @Setter private String sign;

    public ChangeMasterCommand(long term, String masterName, Map<String, ChangeMasterVerifyResponse> value) {
        super(value);
        this.term = term;
        this.masterName = masterName;
    }

    @Override public String getNodeName() {
        return masterName;
    }

    @Override public String getSignValue() {
        StringBuffer sb = new StringBuffer();
        sb.append(term).append(masterName);
        get().values().stream().sorted(Comparator.comparing(ChangeMasterVerifyResponse::getVoter))
            .forEach(r -> sb.append(r.getSign()));
        return Hashing.sha256().hashString(sb.toString(), Charsets.UTF_8).toString();
    }

    @Override public String getSignature() {
        return sign;
    }
}
