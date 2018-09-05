/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import com.higgs.trust.config.node.command.ViewCommand;

import java.util.List;

public interface IClusterViewManager {

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
     * get current view id
     */
    long getCurrentViewId();

    /**
     * get the {@link ClusterView} at height
     */
    ClusterView getViewWithHeight(long height);

    /**
     * get the cluster view by view id, if viewId less then 0, return the currentView
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
