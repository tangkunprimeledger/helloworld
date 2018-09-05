/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master.command;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Getter @Setter @NoArgsConstructor public class ChangeMasterVerifyCmd extends ValidCommand<ChangeMasterVerify> {
    private static final long serialVersionUID = 961625577325944353L;

    private static final String CHANGE_MASTER_VERIFY = "change_master_verify";

    private String requestId;

    public ChangeMasterVerifyCmd(ChangeMasterVerify value) {
        super(value, -1);
        this.requestId = String.join("_", CHANGE_MASTER_VERIFY, "" + value.getTerm(), "" + value.getView(),
            "" + System.currentTimeMillis());
    }

    @Override public String messageDigest() {
        return requestId;
    }
}
