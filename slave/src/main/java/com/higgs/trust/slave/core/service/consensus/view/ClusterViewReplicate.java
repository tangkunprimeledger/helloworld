/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.view;

/**
 * @author suimi
 * @date 2018/6/11
 */

import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@P2pvalidReplicator @Component public class ClusterViewReplicate {

    @Autowired IClusterViewManager viewManager;

    /**
     * handle the cluster info command
     *
     * @param commit
     */
    public ValidClusterViewCmd handleClusterView(ValidSyncCommit<ClusterViewCmd> commit) {
        ClusterViewCmd cmd = commit.operation();
        ClusterView view;
        if (cmd.getView() < 0) {
            view = viewManager.getCurrentView();
        } else {
            view = viewManager.getView(cmd.getView());
        }
        return new ValidClusterViewCmd(cmd.get(), view);
    }
}
