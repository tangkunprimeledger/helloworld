package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.config.ClusterConfigDao;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.dao.rocks.config.ClusterConfigRocksDao;
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
    @Autowired private ClusterConfigRocksDao clusterConfigRocksDao;
    @Autowired private InitConfig initConfig;

    /**
     * @param clusterConfig
     * @return
     * @desc insert clusterConfig into db
     */
    public void insertClusterConfig(ClusterConfig clusterConfig) {
        ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
        BeanUtils.copyProperties(clusterConfig, clusterConfigPO);
        if (initConfig.isUseMySQL()) {
            clusterConfigDao.insertClusterConfig(clusterConfigPO);
        } else {
            clusterConfigRocksDao.saveWithTransaction(clusterConfigPO);
        }
    }

    /**
     * @param clusterName
     * @return ClusterConfig
     * @desc get ClusterConfig by cluster name
     */
    public ClusterConfig getClusterConfig(String clusterName) {
        ClusterConfigPO clusterConfigPO;
        if (initConfig.isUseMySQL()) {
            clusterConfigPO = clusterConfigDao.getClusterConfig(clusterName);
        } else {
            clusterConfigPO = clusterConfigRocksDao.get(clusterName);
        }

        if (null == clusterConfigPO) {
            return null;
        }
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
        int affectRows;
        if (initConfig.isUseMySQL()) {
            try {
                affectRows = clusterConfigDao.batchInsert(clusterConfigPOList);
            } catch (DuplicateKeyException e) {
                log.error(
                    "batch insert clusterConfig fail, because there is DuplicateKeyException for clusterConfigPOList:",
                    clusterConfigPOList);
                throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
            }
        } else {
            affectRows = clusterConfigRocksDao.batchInsert(clusterConfigPOList);
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
        if (initConfig.isUseMySQL()) {
            return clusterConfigPOList.size() == clusterConfigDao.batchUpdate(clusterConfigPOList);
        }
        return clusterConfigPOList.size() == clusterConfigRocksDao.batchInsert(clusterConfigPOList);
    }
}
