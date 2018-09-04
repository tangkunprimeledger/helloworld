/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suimi
 * @date 2018/8/31
 */
@Slf4j @Component public class ClusterViewManager implements IClusterViewManager {

    private ClusterView currentView;

    private List<ClusterView> views = new ArrayList<>();

    @Override public synchronized void resetViews(List<ClusterView> views) {
        this.views = views;
        currentView = views.get(views.size() - 1);
    }

    @Override public List<ClusterView> getViews() {
        return views;
    }

    @Override public ClusterView getCurrentView() {
        return currentView;
    }

    @Override public ClusterView getViewWithHeight(long height) {
        for (ClusterView view : views) {
            if (view.getEndHeight() == ClusterView.INIT_END_HEIGHT) {
                if (height == view.getStartHeight()) {
                    return view;
                }
            } else if (height >= view.getStartHeight() && height <= view.getEndHeight()) {
                return view;
            }
        }
        return null;
    }

    @Override public ClusterView getView(long viewId) {
        for (ClusterView view : views) {
            if (viewId == view.getId()) {
                return view;
            }
        }
        return null;
    }

    @Override public void initViews(ClusterView clusterView) {
        views.clear();
        views.add(clusterView);
        currentView = clusterView;
    }

    @Override public void changeView(ClusterOptTx optTx) {

    }

    @Override public void resetEndHeight(long[] packageHeights) {
        int len = packageHeights.length;
        boolean verify = currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT ?
            packageHeights[0] == currentView.getStartHeight() : packageHeights[0] == currentView.getEndHeight() + 1
            && packageHeights[len - 1] <= currentView.getEndHeight() + len;
        if (!verify) {
            throw new ConfigException(ConfigError.CONFIG_VIEW_PACKAGE_HEIGHT_INCORRECT);
        }
        if ((packageHeights[0] == currentView.getStartHeight()
            && currentView.getEndHeight() == ClusterView.INIT_END_HEIGHT) || (
            packageHeights[0] == currentView.getEndHeight() + 1
                && packageHeights[len - 1] == currentView.getEndHeight() + len)) {
            log.debug("reset currentView end height:{}", packageHeights[len - 1]);
            currentView.setEndHeight(packageHeights[len - 1]);
        } else {
            log.warn("set incorrect end height:{}, currentView:{}", packageHeights[len - 1], currentView);
        }
    }
}
