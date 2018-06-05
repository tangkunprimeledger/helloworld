/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus.master;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @author suimi
 * @date 2018/6/5
 */
@Data @NoArgsConstructor @RequiredArgsConstructor public class ChangeMasterVerifyResponse implements Serializable {

    private static final long serialVersionUID = -6091022260408731431L;

    /**
     * the term number
     */
    @NonNull private long term;

    /**
     * the node name of voter
     */
    @NonNull private String voter;

    /**
     * the node name of proposer
     */
    @NonNull private String proposer;

    /**
     * package height
     */
    @NonNull private long packageHeight;

    /**
     * if change the master
     */
    @NonNull private boolean changeMaster;

    /**
     * signature
     */
    private String sign;

    public String getSignValue() {
        return String.join(",", "" + term, voter, proposer, "" + packageHeight, "" + changeMaster);
    }
}
