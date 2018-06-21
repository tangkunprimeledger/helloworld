/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.p2pvalid.config;

import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.p2pvalid.core.IdValidCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/19
 */
@NoArgsConstructor @Getter @Setter public class ValidClusterInfoCmd extends IdValidCommand<ClusterInfoVo> {

    private static final long serialVersionUID = -3243023833695624710L;

    public ValidClusterInfoCmd(String requestId, ClusterInfoVo clusterInfoVo) {
        super(requestId, clusterInfoVo);
    }

    @Override public String messageDigest() {
        StringBuilder sb = new StringBuilder();
        get().getClusters().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
            .forEach(entry -> sb.append(entry.getKey()).append(entry.getValue()));
        return String.join(",", getRequestId(), "" + get().getFaultNodeNum(), sb.toString());
    }
}
