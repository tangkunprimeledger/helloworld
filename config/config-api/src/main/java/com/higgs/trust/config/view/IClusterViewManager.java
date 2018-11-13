/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import com.higgs.trust.config.node.command.ViewCommand;

import java.util.List;

public interface IClusterViewManager {

    long CURRENT_VIEW_ID = -1;

    /**
     * reset the cluster views
     */
    void resetViews(List<ClusterView> views);

    /**
     * get the cluster views
     */
    List<ClusterView> getViews();

    /**
     * get current view, if null will be return start view
     */
    ClusterView getCurrentView();

    /**
     * get current view id
     */
    default long getCurrentViewId() {
        return getCurrentView().getId();
    }

    /**
     * get the {@link ClusterView} at height
     */
    ClusterView getViewWithHeight(long height);

    /**
     * get the cluster view by view id, if viewId is {@link IClusterViewManager#CURRENT_VIEW_ID}, return the currentView.
     */
    ClusterView getView(long viewId);

    /**
     * change cluster view
     */
    void changeView(ViewCommand command);

    /**
     * reset the end height of current view
     * @param height
     */
    void resetEndHeight(long height);

    /**
     * get the time of last package
     */
    Long getLastPackTime();

    /**
     * reset the time of last package
     */
    void resetLastPackTime(long packTime);
}
