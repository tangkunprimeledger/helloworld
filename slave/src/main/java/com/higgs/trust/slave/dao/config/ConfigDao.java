package com.higgs.trust.slave.dao.config;

import com.higgs.trust.slave.dao.po.config.ConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 14:16
 */
@Mapper public interface ConfigDao {

    /**
     * @param configPO
     * @return
     * @desc insert config into db
     */
    void insertConfig(ConfigPO configPO);

    /**
     * @param configPO
     * @return
     * @desc update config information
     */
    void updateConfig(ConfigPO configPO);

    /**
     * @param configPO
     * @return ConfigPO
     * @desc get config information by nodeName
     */
    ConfigPO getConfig(ConfigPO configPO);
}
