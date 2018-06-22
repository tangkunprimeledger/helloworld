package com.higgs.trust.config.node.listener;

public interface MasterChangeListener {

    void beforeChange(String masterName);

    /**
     * 节点 master 变更
     *
     * @param masterName 新的master名
     */
    void masterChanged(String masterName);
}
