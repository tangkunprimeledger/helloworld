package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.dao.config.SystemPropertyDao;
import com.higgs.trust.slave.dao.po.config.SystemPropertyPO;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * query system property by key
     *
     * @param key
     * @return
     */
    public SystemProperty queryByKey(String key) {
        SystemProperty systemProperty = null;
        SystemPropertyPO systemPropertyPO = systemPropertyDao.queryByKey(key);
        if (null != systemPropertyPO) {
            systemProperty = new SystemProperty();
            BeanUtils.copyProperties(systemPropertyPO, systemProperty);
        }
        return systemProperty;
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
        systemPropertyDao.add(systemPropertyPO);
    }

    /**
     * add property into db
     * @param key
     * @param value
     * @return
     */
    public int update(String key, String value){
        return systemPropertyDao.update(key, value);
    }

}
