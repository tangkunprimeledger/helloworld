/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.config.node.command.ViewCommand;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.config.view.LastPackage;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/9/4
 */
@Order(2) @Component @Slf4j public class ClusterViewFilter implements CommandFilter {

    @Autowired private NodeState nodeState;

    @Autowired private IClusterViewManager viewManager;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof ViewCommand) {
            ViewCommand command = (ViewCommand)commit.operation();
            LastPackage lastPackage = viewManager.getLastPackage();
            long height = command.getPackageHeight();
            long packTime = command.getPackageTime();
            if (height > lastPackage.getHeight() && packTime < lastPackage.getTime()) {
                log.warn("the package time:{} of package:{} later than lastPackage:{}", packTime, height, lastPackage);
                commit.close();
                return;
            }
            ClusterView currentView = viewManager.getCurrentView();
            //not current view
            if (command.getView() != currentView.getId()) {
                ClusterView view = viewManager.getView(command.getView());
                if (view == null) {
                    log.warn("the view:{} not exist, please check", command.getView());
                    commit.close();
                    return;
                }
                if (!(height >= view.getStartHeight() && height <= view.getEndHeight())) {
                    log.warn("the height:{} not allowed at view:{}", height, view);
                    commit.close();
                    return;
                }
            } else {
                //the height continuous with current view
                if (height == currentView.getEndHeight() + 1 || (height == currentView.getStartHeight()
                    && currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT)) {
                    viewManager.resetEndHeight(height);
                    viewManager.resetLastPackage(new LastPackage(height, packTime));
                    if (command.getClusterOptTx() != null) {
                        viewManager.changeView(command);
                    }
                } else {
                    //the height not all in current view
                    if (!(height >= currentView.getStartHeight() && height <= currentView.getEndHeight())) {
                        log.warn("the height:{} out of current view:{}", height, currentView);
                        commit.close();
                        return;
                    }
                }
            }
        }
        chain.doFilter(commit);
    }
}
