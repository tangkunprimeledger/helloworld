/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/8/31
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class ClusterView {

    public static final long INIT_END_HEIGHT = -1;

    private long id;

    private int faultNum;

    private long startHeight;

    private long endHeight;

    private Map<String, String> nodes = new HashMap<>();

    public List<String> getNodeNames() {
        ArrayList<String> nodeNames = new ArrayList<>();
        nodeNames.addAll(nodes.keySet());
        return nodeNames;
    }



}
