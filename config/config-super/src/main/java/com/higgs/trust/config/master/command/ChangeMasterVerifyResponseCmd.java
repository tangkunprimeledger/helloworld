/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master.command;

import com.higgs.trust.consensus.p2pvalid.core.IdValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Getter @Setter @NoArgsConstructor public class ChangeMasterVerifyResponseCmd
    extends IdValidCommand<ChangeMasterVerifyResponse> {
    private static final long serialVersionUID = 7506595686406239636L;

    public ChangeMasterVerifyResponseCmd(String requestId, ChangeMasterVerifyResponse changeMasterVerifyResponse) {
        super(requestId, changeMasterVerifyResponse);
    }

    @Override public String messageDigest() {
        return getRequestId() + get().isChangeMaster();
    }
}
