/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.node.command;

import com.higgs.trust.config.view.ClusterOptTx;

/**
 * @author suimi
 * @date 2018/9/4
 */
public interface ViewCommand {

    /**
     * get the view id
     */
    long getView();

    /**
     * get the package height
     */
    long getPackageHeight();

    /**
     * get the package time
     */
    long getPackageTime();

    /**
     * get the cluster operation transaction {@link ClusterOptTx}
     */
    ClusterOptTx getClusterOptTx();

}
