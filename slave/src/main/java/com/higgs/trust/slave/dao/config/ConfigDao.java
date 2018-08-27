package com.higgs.trust.slave.dao.config;

import com.higgs.trust.slave.dao.po.config.ConfigPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
     * @return List
     * @desc get config information by nodeName and usage(if needed)
     */
    List<ConfigPO> getConfig(ConfigPO configPO);

    /**
     * batch insert
     *
     * @param configPOList
     * @return
     */
    int batchInsert(List<ConfigPO> configPOList);

    /**
     * batch update
     *
     * @param configPOList
     * @return
     */
    int batchUpdate(List<ConfigPO> configPOList);
}
