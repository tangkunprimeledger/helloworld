/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.term;

import lombok.*;

/**
 * @author suimi
 * @date 2018/6/1
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class TermInfo {

    public static final long INIT_END_HEIGHT = -1;
    private long term;

    private String masterName;

    private long startHeight;

    private long endHeight;

}
