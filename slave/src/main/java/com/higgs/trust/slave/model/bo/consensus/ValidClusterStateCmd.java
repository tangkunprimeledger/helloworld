/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.p2pvalid.core.IdValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liuyu
 * @date 2018/10/12
 */
@Getter @Setter @NoArgsConstructor public class ValidClusterStateCmd extends IdValidCommand<NodeStateEnum> {

    private static final long serialVersionUID = -7652400642865085127L;

    public ValidClusterStateCmd(String id, NodeStateEnum state) {
        super(id, state);
    }

    @Override public String messageDigest() {
        return getRequestId() + Constant.SPLIT_SLASH + get();
    }
}
