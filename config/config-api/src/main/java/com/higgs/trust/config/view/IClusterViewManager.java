/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

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
     * get the {@link ClusterView} at height
     */
    ClusterView getViewWithHeight(long height);

    /**
     * get the cluster view by view id
     */
    ClusterView getView(long viewId);

    /**
     * init the cluster view
     */
    void initViews(ClusterView clusterView);

    /**
     * change cluster view
     */
    void changeView(ClusterOptTx optTx);

    /**
     * reset the end height of current view
     */
    void resetEndHeight(long[] height);
}
