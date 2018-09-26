package com.higgs.trust.slave.dao.mysql.config;

import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /**
     * batch insert
     *
     * @param clusterConfigPOList
     * @return
     */
    int batchInsert(List<ClusterConfigPO> clusterConfigPOList);

    /**
     * batch update
     *
     * @param clusterConfigPOList
     * @return
     */
    int batchUpdate(List<ClusterConfigPO> clusterConfigPOList);
}
