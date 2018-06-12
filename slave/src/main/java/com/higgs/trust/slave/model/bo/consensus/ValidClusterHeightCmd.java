/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.IdValidCommand;
import com.higgs.trust.slave.common.constant.Constant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@Getter @Setter @NoArgsConstructor public class ValidClusterHeightCmd extends IdValidCommand<Long> {

    private static final long serialVersionUID = -7652400642865085127L;

    public ValidClusterHeightCmd(String id, Long height) {
        super(id, height);
    }

    @Override public String messageDigest() {
        return getRequestId() + Constant.SPLIT_SLASH + get();
    }
}
