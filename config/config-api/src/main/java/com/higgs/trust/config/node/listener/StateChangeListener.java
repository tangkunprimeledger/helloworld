package com.higgs.trust.config.node.listener;

import com.higgs.trust.config.node.NodeStateEnum;

public interface StateChangeListener {
    /**
     * 节点状态发生变更
     *
     * @param from 原始状态
     * @param to   新状态
     */
    void stateChanged(NodeStateEnum from, NodeStateEnum to);
}
