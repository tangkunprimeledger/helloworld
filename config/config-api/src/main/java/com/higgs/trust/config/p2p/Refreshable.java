/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.p2p;

public interface Refreshable {

    /**
     * set to refresh
     */
    void setRefresh();

    /**
     * if need refresh
     */
    boolean needRefresh();

    /**
     * refresh if need
     */
    void refreshIfNeed();

    /**
     * refresh
     */
    void refresh();
}
