/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

/**
 * @author suimi
 * @date 2018/6/12
 */
public interface INodeInfoService {

    Long packageHeight();

    Long blockHeight();

    /**
     * is the current node  qualified for master
     *
     * @return
     */
    boolean hasMasterQualify();
}
