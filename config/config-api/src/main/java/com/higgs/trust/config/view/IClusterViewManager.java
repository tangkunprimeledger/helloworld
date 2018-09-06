/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import com.higgs.trust.config.node.command.ViewCommand;

import java.util.List;

public interface IClusterViewManager {

    long CURRENT_VIEW_ID = -1;

    long START_CLUSTER_VIEW_ID = -2;

    /**
     * reset the cluster views
     */
    void resetViews(List<ClusterView> views);

    /**
     * get the cluster views
     */
    List<ClusterView> getViews();

    /**
     * get current view
     */
    ClusterView getCurrentView();

    /**
     * get the cluster current view, it's init at starting
     */
    ClusterView getStartView();

    /**
     * set the start view
     */
    void setStartView(ClusterView clusterView);

    /**
     * get current view id
     */
    long getCurrentViewId();

    /**
     * get the {@link ClusterView} at height
     */
    ClusterView getViewWithHeight(long height);

    /**
     * get the cluster view by view id, if viewId is {@link IClusterViewManager#CURRENT_VIEW_ID}, return the currentView.
     * if viewId is {@link IClusterViewManager#START_CLUSTER_VIEW_ID}, return the cluster current view
     */
    ClusterView getView(long viewId);

    /**
     * init the cluster view
     */
    void initViews(ClusterView clusterView);

    /**
     * change cluster view
     */
    void changeView(ViewCommand command);

    /**
     * reset the end height of current view
     */
    void resetEndHeight(long[] height);
}
