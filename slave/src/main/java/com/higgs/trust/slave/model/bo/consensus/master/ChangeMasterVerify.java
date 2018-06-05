/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus.master;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Data @Builder @AllArgsConstructor public class ChangeMasterVerify implements Serializable {

    private static final long serialVersionUID = 429236225982859241L;

    /**
     * the term number
     */
    private long term;

    /**
     * the node name of proposer
     */
    private String proposer;

    /**
     * the package height
     */
    private long packageHeight;

}
