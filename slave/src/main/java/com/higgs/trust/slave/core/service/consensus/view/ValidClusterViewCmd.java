/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.view;

import com.higgs.trust.config.view.ClusterView;
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
@NoArgsConstructor @Getter @Setter public class ValidClusterViewCmd extends IdValidCommand<ClusterView> {

    private static final long serialVersionUID = -3243023833695624710L;

    public ValidClusterViewCmd(String requestId, ClusterView clusterView) {
        super(requestId, clusterView);
    }

    @Override public String messageDigest() {
        StringBuilder sb = new StringBuilder();
        get().getNodes().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
            .forEach(entry -> sb.append(entry.getKey()).append(entry.getValue()));
        return String.join(",", getRequestId(), "" + get().getFaultNum(), sb.toString());
    }
}
