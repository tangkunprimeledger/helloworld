/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.core;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Getter @Setter @Builder @NoArgsConstructor public class TermInfo {
    private long term;

    private String masterName;

    private long startHeight;

    private long endHeight;

}
