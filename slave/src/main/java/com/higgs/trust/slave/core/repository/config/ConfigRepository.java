package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.config.ConfigDao;
import com.higgs.trust.slave.dao.po.config.ConfigPO;
import com.higgs.trust.slave.dao.rocks.config.ConfigRocksDao;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:11
 */
@Repository @Slf4j public class ConfigRepository {

    @Autowired private ConfigDao configDao;
    @Autowired private ConfigRocksDao configRocksDao;
    @Autowired private InitConfig initConfig;

    /**
     * @param config
     * @return
     * @desc insert config into db
     */
    public void insertConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);
        if (initConfig.isUseMySQL()) {
            configDao.insertConfig(configPO);
        } else {
            configRocksDao.save(configPO);
        }
    }

    /**
     * @param config
     * @return
     * @desc update config information
     */
    public void updateConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);
        if (initConfig.isUseMySQL()) {
            configDao.updateConfig(configPO);
        } else {
            configRocksDao.update(configPO);
        }
    }

    /**
     * @param config
     * @return List<ConfigPO>
     * @desc get config information by nodeName and usage(if needed)
     */
    public List<Config> getConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);

        List<ConfigPO> list;
        if (initConfig.isUseMySQL()) {
            list = configDao.getConfig(configPO);
        } else {
            list = configRocksDao.getConfig(config.getNodeName(), config.getUsage());
        }

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<Config> configList = new LinkedList<>();
        for (ConfigPO configPO1 : list) {
            Config config1 = new Config();
            BeanUtils.copyProperties(configPO1, config1);
            configList.add(config1);
        }
        return configList;
    }

    public Config getBizConfig(String user) {
        ConfigPO configPO = new ConfigPO();
        configPO.setNodeName(user);
        configPO.setUsage(UsageEnum.BIZ.getCode());

        Config config = new Config();
        if (initConfig.isUseMySQL()) {
            List<ConfigPO> list = configDao.getConfig(configPO);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            if (list.size() > 1) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_CA_UPDATE_ERROR, "more than one pair of pub/priKey");
            }
            BeanUtils.copyProperties(list.get(0), config);
        } else {
            BeanUtils.copyProperties(configRocksDao.get(user + "_" + UsageEnum.BIZ.getCode()), config);
        }
        return config;
    }

    /**
     * batch insert
     *
     * @param configPOList
     * @return
     */
    public boolean batchInsert(List<ConfigPO> configPOList) {
        int affectRows;
        if (initConfig.isUseMySQL()) {
            try {
                affectRows = configDao.batchInsert(configPOList);
            } catch (DuplicateKeyException e) {
                log.error("batch insert config fail, because there is DuplicateKeyException for configPOList:", configPOList);
                throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
            }
        } else {
            affectRows = configRocksDao.batchInsert(configPOList);
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
        if (initConfig.isUseMySQL()) {
            return configPOList.size() == configDao.batchUpdate(configPOList);
        }
        return configPOList.size() == configRocksDao.batchInsert(configPOList);
    }
}
