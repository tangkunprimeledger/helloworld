package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.config.ClusterConfigDao;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        BeanUtils.copyProperties(clusterConfig, clusterConfigPO);
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
     * @param clusterName
     * @return ClusterConfig
     * @desc get ClusterConfig by cluster name
     */
    public ClusterConfig getClusterConfig(String clusterName) {
        ClusterConfigPO clusterConfigPO = clusterConfigDao.getClusterConfig(clusterName);
        ClusterConfig clusterConfig = new ClusterConfig();
        BeanUtils.copyProperties(clusterConfigPO, clusterConfig);
        return clusterConfig;
    }

    /**
     * batch insert
     *
     * @param clusterConfigPOList
     * @return
     */
    public boolean batchInsert(List<ClusterConfigPO> clusterConfigPOList) {
        int affectRows = 0;
        try {
            affectRows = clusterConfigDao.batchInsert(clusterConfigPOList);
        } catch (DuplicateKeyException e) {
            log.error(
                "batch insert clusterconfig fail, because there is DuplicateKeyException for clusterConfigPOList:",
                clusterConfigPOList);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        return affectRows == clusterConfigPOList.size();
    }

    /**
     * batch update
     *
     * @param clusterConfigPOList
     * @return
     */
    public boolean batchUpdate(List<ClusterConfigPO> clusterConfigPOList) {
        return clusterConfigPOList.size() == clusterConfigDao.batchUpdate(clusterConfigPOList);
    }
}
