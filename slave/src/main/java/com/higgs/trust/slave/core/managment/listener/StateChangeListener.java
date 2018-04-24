package com.higgs.trust.slave.core.managment.listener;

import com.higgs.trust.slave.common.enums.NodeStateEnum;

public interface StateChangeListener {
    /**
     * 节点状态发生变更
     * @param from 原始状态
     * @param to 新状态
     */
    void stateChanged(NodeStateEnum from, NodeStateEnum to);
}
