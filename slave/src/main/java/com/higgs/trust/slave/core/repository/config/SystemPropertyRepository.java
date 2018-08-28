package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.mysql.config.SystemPropertyDao;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;
import com.higgs.trust.slave.dao.rocks.config.SystemPropertyRocksDao;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import org.rocksdb.RocksDBException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * System  Property  repository
 *
 * @author lingchao
 * @create 2018年06月27日15:58
 */
@Service
public class SystemPropertyRepository {
    @Autowired
    private SystemPropertyDao systemPropertyDao;

    @Autowired
    private SystemPropertyRocksDao systemPropertyRocksDao;

    @Autowired
    private InitConfig initConfig;

    /**
     * query system property by key
     *
     * @param key
     * @return
     */
    public SystemProperty queryByKey(String key) {
        SystemPropertyPO systemPropertyPO;
        if (initConfig.isUseMySQL()) {
            systemPropertyPO = systemPropertyDao.queryByKey(key);
        } else {
            systemPropertyPO = systemPropertyRocksDao.get(key);
        }
        return BeanConvertor.convertBean(systemPropertyPO, SystemProperty.class);
    }

    /**
     * add property into db
     * @param key
     * @param value
     * @param desc
     * @return
     */
    public void add(String key, String value, String desc){
        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setKey(key);
        systemPropertyPO.setValue(value);
        systemPropertyPO.setDesc(desc);

        if (initConfig.isUseMySQL()) {
            systemPropertyDao.add(systemPropertyPO);
        } else {
            systemPropertyRocksDao.put(key, systemPropertyPO);
        }
    }

    /**
     * add property into db
     * @param key
     * @param value
     * @return
     */
    public int update(String key, String value){
        if (initConfig.isUseMySQL()) {
            return systemPropertyDao.update(key, value);
        } else {
            SystemPropertyPO po = systemPropertyRocksDao.get(key);
            if (null != po) {
                po.setValue(value);
                po.setUpdateTime(new Date());
                systemPropertyRocksDao.put(key, po);
                return 1;
            }
            return 0;
        }
    }
}
