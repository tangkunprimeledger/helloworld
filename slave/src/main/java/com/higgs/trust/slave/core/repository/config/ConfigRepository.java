package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.config.ConfigDao;
import com.higgs.trust.slave.dao.po.config.ConfigPO;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
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
     * @param config
     * @return List<ConfigPO>
     * @desc get config information by nodeName and usage(if needed)
     */
    public List<Config> getConfig(Config config) {
        ConfigPO configPO = new ConfigPO();
        BeanUtils.copyProperties(config, configPO);
        List<ConfigPO> list = configDao.getConfig(configPO);
        if (null == list || list.size() == 0) {
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

    /**
     * get biz config
     * @param user
     * @return
     */
    public Config getBizConfig(String user) {
        return getConfig(user,UsageEnum.BIZ);
    }

    /**
     * get config by nodeName and usage
     *
     * @param user
     * @param usageEnum
     * @return
     */
    public Config getConfig(String user,UsageEnum usageEnum) {
        ConfigPO configPO = new ConfigPO();
        configPO.setNodeName(user);
        configPO.setUsage(usageEnum.getCode());
        List<ConfigPO> list = configDao.getConfig(configPO);
        if (null == list || list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_UPDATE_ERROR, "more than one pair of pub/priKey");
        }
        Config config = new Config();
        BeanUtils.copyProperties(list.get(0), config);
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
