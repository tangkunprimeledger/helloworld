/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus.master;

import com.higgs.trust.slave.model.bo.consensus.IdValidCommand;

/**
 * @author suimi
 * @date 2018/6/5
 */
public class ChangeMasterVerifyResponseCmd extends IdValidCommand<ChangeMasterVerifyResponse> {
    private static final long serialVersionUID = 7506595686406239636L;

    public ChangeMasterVerifyResponseCmd(String requestId, ChangeMasterVerifyResponse changeMasterVerifyResponse) {
        super(requestId, changeMasterVerifyResponse);
    }

    @Override public String messageDigest() {
        return getRequestId() + get().isChangeMaster();
    }
}
