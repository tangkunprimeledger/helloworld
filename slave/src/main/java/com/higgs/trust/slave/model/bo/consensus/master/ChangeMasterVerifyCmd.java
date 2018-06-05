/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus.master;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.model.bo.BlockHeader;

/**
 * @author suimi
 * @date 2018/6/5
 */
public class ChangeMasterVerifyCmd extends ValidCommand<ChangeMasterVerify> {
    private static final long serialVersionUID = 961625577325944353L;

    private static final String CHANGE_MASTER_VERIFY = "change_master_verify";

    private String requestId;

    public ChangeMasterVerifyCmd(ChangeMasterVerify value) {
        super(value);
        this.requestId = String
            .join(Constant.SPLIT_SLASH, CHANGE_MASTER_VERIFY, "" + value.getTerm(), "" + System.currentTimeMillis());
    }

    @Override public String messageDigest() {
        return requestId;
    }
}
