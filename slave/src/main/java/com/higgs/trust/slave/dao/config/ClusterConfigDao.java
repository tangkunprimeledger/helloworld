package com.higgs.trust.slave.dao.config;

import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 14:16
 */
@Mapper public interface ClusterConfigDao {

    /**
     * @param clusterConfigPO
     * @return
     * @desc insert clusterConfig into db
     */
    void insertClusterConfig(ClusterConfigPO clusterConfigPO);

    /**
     * @param clusterConfigPO
     * @return
     * @desc update ClusterConfig
     */
    void updateClusterConfig(ClusterConfigPO clusterConfigPO);

    /**
     * @param clusterName
     * @return ClusterConfigPO
     * @desc get ClusterConfig by cluster name
     */
    ClusterConfigPO getClusterConfig(String clusterName);
}
