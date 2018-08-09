package com.higgs.trust.slave.dao.config;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * System  Property  dao
 * @author lingchao
 * @create 2018年06月27日15:58
 */
@Mapper
public interface SystemPropertyDao extends BaseDao<SystemPropertyPO> {

    /**
     * query  system property by key
     * @param key
     * @return
     */
    SystemPropertyPO queryByKey(@Param("key") String key);

    /**
     * query  system property by key
     * @param key
     * @return
     */
    int update(@Param("key") String key, @Param("value") String value);

}
