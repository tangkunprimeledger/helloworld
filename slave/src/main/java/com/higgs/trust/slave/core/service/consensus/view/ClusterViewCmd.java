/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.view;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.NoArgsConstructor;

/**
 * @author suimi
 * @date 2018/6/19
 */
@NoArgsConstructor public class ClusterViewCmd extends ValidCommand<String> {

    private static final long serialVersionUID = -7729848938347712491L;

    public ClusterViewCmd(String requestId, long view) {
        super(requestId, view);
    }

    @Override public String messageDigest() {
        return get();
    }
}
