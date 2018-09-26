/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Data @Builder @AllArgsConstructor @NoArgsConstructor public class ChangeMasterVerify implements Serializable {

    private static final long serialVersionUID = 429236225982859241L;

    /**
     * the term number
     */
    private long term;

    /**
     * the cluster view number
     */
    private long view;

    /**
     * the node name of proposer
     */
    private String proposer;

    /**
     * the package height
     */
    private long packageHeight;

}
