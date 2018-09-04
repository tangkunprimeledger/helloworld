/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.filter;

import com.higgs.trust.config.node.command.ViewCommand;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import com.higgs.trust.consensus.core.filter.CommandFilter;
import com.higgs.trust.consensus.core.filter.CommandFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author suimi
 * @date 2018/9/4
 */
@Order @Component @Slf4j public class ClusterViewFilter implements CommandFilter {

    @Autowired private IClusterViewManager viewManager;

    @Override
    public void doFilter(ConsensusCommit<? extends AbstractConsensusCommand> commit, CommandFilterChain chain) {
        if (commit.operation() instanceof ViewCommand) {
            ViewCommand command = (ViewCommand)commit.operation();
            long[] heights = command.getPackageHeight();
            if (ArrayUtils.isEmpty(heights) || !checkHeightContinuous(heights)) {
                log.warn("package command heights is not continuous or empty, heights={}", heights);
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
                long[] unMatchedHeights = Arrays.stream(heights)
                    .filter(height -> height >= view.getStartHeight() && height <= view.getEndHeight()).toArray();
                if (unMatchedHeights.length > 0) {
                    log.warn("the heights:{} not allowed at view:{}", unMatchedHeights, view);
                    commit.close();
                    return;
                }
            } else {
                //the heights continuous with current view
                if (heights[0] == currentView.getEndHeight() + 1 || (heights[0] == currentView.getStartHeight()
                    && currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT)) {
                    viewManager.resetEndHeight(heights);
                } else {
                    long[] unMatchedHeights = Arrays.stream(heights).filter(
                        height -> height >= currentView.getStartHeight() && height <= currentView.getEndHeight())
                        .toArray();
                    //the heights not all in current view
                    if (unMatchedHeights.length > 0) {
                        log.warn("the heights:{} out of current view:{}", unMatchedHeights, currentView);
                        commit.close();
                        return;
                    }
                }
                if (command.getClusterOptTx() != null) {
                    viewManager.changeView(command);
                }
            }
        }
        chain.doFilter(commit);
    }

    private boolean checkHeightContinuous(long[] height) {
        for (int i = 0; i < height.length - 1; i++) {
            if ((height[i] + 1) != height[i + 1]) {
                return false;
            }
        }
        return true;
    }
}
