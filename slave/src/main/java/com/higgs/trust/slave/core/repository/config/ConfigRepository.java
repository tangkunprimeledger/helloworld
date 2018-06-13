package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.config.ConfigDao;
import com.higgs.trust.slave.dao.po.config.ConfigPO;
import com.higgs.trust.slave.model.bo.config.Config;
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
@Repository @Slf4j public class ConfigRepository {

    @Autowired private ConfigDao configDao;

    /**
     * @param config
     * @return
     * @desc insert config into db
     */
    public void insertConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);
        configDao.insertConfig(configPO);
    }

    /**
     * @param config
     * @return
     * @desc update config information
     */
    public void updateConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);
        configDao.updateConfig(configPO);
    }

    /**
     * @param nodeName
     * @return Config
     * @desc get config information by nodeName
     */
    public Config getConfig(String nodeName) {
        ConfigPO configPO = new ConfigPO();
        Config config = new Config();
        configPO.setNodeName(nodeName);
        configPO = configDao.getConfig(configPO);
        BeanUtils.copyProperties(configPO, config);
        return config;
    }

    /**
     * batch insert
     *
     * @param configPOList
     * @return
     */
    public boolean batchInsert(List<ConfigPO> configPOList) {
        int affectRows = 0;
        try {
            affectRows = configDao.batchInsert(configPOList);
        } catch (DuplicateKeyException e) {
            log.error("batch insert ca fail, because there is DuplicateKeyException for caPOList:", configPOList);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        return affectRows == configPOList.size();
    }

    /**
     * batch update
     *
     * @param configPOList
     * @return
     */
    public boolean batchUpdate(List<ConfigPO> configPOList) {
        return configPOList.size() == configDao.batchUpdate(configPOList);
    }
}
