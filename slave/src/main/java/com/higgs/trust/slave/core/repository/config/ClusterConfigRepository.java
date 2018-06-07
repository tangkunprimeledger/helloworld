package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.dao.config.ClusterConfigDao;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:11
 */
@Repository @Slf4j public class ClusterConfigRepository {

    @Autowired private ClusterConfigDao clusterConfigDao;

    /**
     * @param clusterConfig
     * @return
     * @desc insert clusterConfig into db
     */
    public void insertClusterConfig(ClusterConfig clusterConfig) {
        ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
        BeanUtils.copyProperties(clusterConfig,clusterConfigPO);
        clusterConfigDao.insertClusterConfig(clusterConfigPO);
    }

    /**
     * @param clusterConfig
     * @return
     * @desc update ClusterConfig
     */
    public void updateClusterConfig(ClusterConfig clusterConfig) {

    }

    /**
     * @param clusterConfig
     * @return ClusterConfig
     * @desc get ClusterConfig by cluster name
     */
    public ClusterConfig getClusterConfig(ClusterConfig clusterConfig) {
        return null;
    }
}
