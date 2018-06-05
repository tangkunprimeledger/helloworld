/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus.master;

import com.alibaba.fastjson.annotation.JSONField;
import com.higgs.trust.consensus.core.command.SignatureCommand;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author suimi
 * @date 2018/6/5
 */
public class MasterHeartbeatCommand extends SignatureCommand<Long> {

    private static final long serialVersionUID = 4579567364750332581L;
    /**
     * master name
     */
    private String masterName;

    /**
     * signature
     */
    @NotEmpty @JSONField(label = "sign") private String sign;

    public MasterHeartbeatCommand(long term, String masterName) {
        super(term);
        this.masterName = masterName;
    }

    @Override public String getNodeName() {
        return masterName;
    }

    @Override public String getSignValue() {
        return masterName + get();
    }

    @Override public String getSignature() {
        return sign;
    }
}
