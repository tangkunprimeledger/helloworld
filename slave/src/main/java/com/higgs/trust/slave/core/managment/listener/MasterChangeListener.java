package com.higgs.trust.slave.core.managment.listener;

public interface MasterChangeListener {

    /**
     * 节点 master 变更
     *
     * @param masterName 新的master名
     */
    void masterChanged(String masterName);
}
