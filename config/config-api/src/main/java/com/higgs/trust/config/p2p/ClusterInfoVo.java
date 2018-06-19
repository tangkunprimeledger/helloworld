/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.p2p;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/6/19
 */
@NoArgsConstructor @Data public class ClusterInfoVo implements Serializable {
    private static final long serialVersionUID = 8408945160119430172L;

    private int faultNodeNum;

    private Map<String, String> clusters;
}
