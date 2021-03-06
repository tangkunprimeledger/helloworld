/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author suimi
 * @date 2018/8/31
 */
@Slf4j @Data @Builder @NoArgsConstructor @AllArgsConstructor public class ClusterView
    implements Cloneable, Serializable {

    public static final long INIT_END_HEIGHT = -1;

    /**
     * the view id
     */
    private long id;

    private int faultNum;

    private long startHeight;

    private long endHeight = INIT_END_HEIGHT;

    private Map<String, String> nodes = new HashMap<>();

    public ClusterView(long id, long startHeight, Map<String, String> nodes) {
        this.id = id;
        this.startHeight = startHeight;
        this.nodes = nodes;
        this.faultNum = (nodes.size() - 1) / 3;
    }

    public List<String> getNodeNames() {
        ArrayList<String> nodeNames = new ArrayList<>();
        nodeNames.addAll(nodes.keySet());
        return nodeNames;
    }

    /**
     * get quorum size for 'apply' peration, like package-persisting, and change-master-confirm
     * AppliedQuorum is (n + f)/2 + 1
     *
     * @param
     * @return quorum size
    */
    public int getAppliedQuorum(){
        int appliedQuprum = (nodes.size()+ faultNum) / 2 + 1;
        return appliedQuprum <= nodes.size() ? appliedQuprum : nodes.size();
    }

    /**
     * get quorum size for 'verify' or 'query' peration,
     * like get-cluster-height, get-safe-height, change-master-verify,
     * and validate-header
     * VerifiedQuorum is f + 1
     *
     * @param
     * @return quorum size
    */
    public int getVerifiedQuorum(){
        int verifiedQuorum = faultNum + 1;
        return verifiedQuorum <= nodes.size() ? verifiedQuorum : nodes.size();
    }

    /**
     * get majority quorum,
     * 4 for 6, 3 for 5, 3 for 4 ...
     *
     * @param
     * @return quorum size
    */
    public int getMajorityQuorum() {
        int majorityQuorum = (nodes.size() + 2) / 2;
        return majorityQuorum <= nodes.size() ? majorityQuorum : nodes.size();
    }

    public String getPubKey(String nodeName) {
        return nodes.get(nodeName);
    }

    @Override public ClusterView clone() {
        try {
            return (ClusterView)super.clone();
        } catch (CloneNotSupportedException e) {
            log.error("clone cluster view error", e);
        }
        return null;
    }
}
