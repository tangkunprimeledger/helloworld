/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.p2pvalid.core.IdValidCommand;
import lombok.NoArgsConstructor;

/**
 * @author suimi
 * @date 2018/6/19
 */
@NoArgsConstructor public class ValidClusterInfoCmd extends IdValidCommand<ClusterInfoVo> {

    private static final long serialVersionUID = -8444604701966607243L;

    public ValidClusterInfoCmd(String requestId, ClusterInfoVo clusterInfoVo) {
        super(requestId, clusterInfoVo);
    }

    @Override public String messageDigest() {
        StringBuilder sb = new StringBuilder();
        get().getClusters().entrySet().stream().sorted()
            .forEach(entry -> sb.append(entry.getKey()).append(entry.getValue()));
        return String.join(",", getRequestId(), "" + get().getFaultNodeNum(), sb.toString());
    }
}
