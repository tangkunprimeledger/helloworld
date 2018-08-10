package com.higgs.trust.slave.core.service.datahandler.manage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.slave.api.vo.SystemPropertyVO;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * SystemProperty snapshot handler
 *
 * @author lingchao
 * @create 2018年06月29日10:51
 */
@Slf4j
@Service
public class SystemPropertyHandler {
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    /**
     * systemProperty loading cache
     */
    LoadingCache<String, SystemProperty> systemPropertyCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(500).refreshAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, SystemProperty>() {
        @Override
        public SystemProperty load(String key) throws Exception {
            log.info("get system property of key :{} from db", key);
            return systemPropertyRepository.queryByKey(key);
        }
    });

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    public SystemPropertyVO querySystemPropertyByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        SystemProperty systemProperty = null;
        try {
            systemProperty = systemPropertyCache.get(key);
        } catch (Throwable e) {
            log.info("Get no data from systemPropertyCache by key:{}", key);
        }
        SystemPropertyVO systemPropertyVO = null;
        if (null != systemProperty) {
            systemPropertyVO = new SystemPropertyVO();
            BeanUtils.copyProperties(systemProperty, systemPropertyVO);
        }
        return systemPropertyVO;
    }


    /**
     * query System Property by key  for command
     *
     * @param key
     * @return
     */
    public String get(String key) {
        if (StringUtils.isBlank(key)) {
            return "Key can not be null!";
        }
        SystemPropertyVO systemPropertyVO = querySystemPropertyByKey(key);
        if (null == systemPropertyVO) {
            return "There is no system property value for key =" + key;
        }
        return "key = " + key + " value = " + systemPropertyVO.getValue();
    }


    /**
     * add property into db
     *
     * @param key
     * @param value
     * @param desc
     * @return
     */
    public String add(String key, String value, String desc) {
        //check arguments
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            log.error("Key and value can not be null. key = {},value = {}", key, value);
            return "Add property into system failed! Key or value can not be null. key = " + key + " ,value = " + value;
        }

        //add data into DB
        try {
            //check data whether in db
            SystemPropertyVO systemPropertyVO = querySystemPropertyByKey(key);
            if (null != systemPropertyVO) {
                log.error("Add property into system failed! There is property in system for key = {} value = {}", key, value);
                return "Add property into system failed! There is property in system. key = " + key + " ,value = " + value;
            }
            systemPropertyRepository.add(key, value, desc);
        } catch (Throwable e) {
            log.error("Add property into system failed!", e);
            return "Add property into system failed!" + e;
        }
        return "Add property into system success! key= " + key + "  value = " + value;
    }


    /**
     * update property into db
     *
     * @param key
     * @param value
     * @return
     */
    public String update(String key, String value) {
        //check arguments
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            log.error("Key and value can not be null. key = {},value = {}", key, value);
            return "Update property into system failed! Key or value can not be null. key = " + key + " ,value = " + value;
        }

        //update data into DB
        try {
            //check data whether in db
            SystemPropertyVO systemPropertyVO = querySystemPropertyByKey(key);
            if (null == systemPropertyVO) {
                log.error("Update property into system failed! There is no property in system for key = {}", key);
                return "Update property into system failed! There is no property in system. key = " + key + " ,value = " + value;
            }

            //update property
            int updateRows = systemPropertyRepository.update(key, value);
            if (1 != updateRows) {
                log.error("Update property into system failed!Key = {} value = {}", key, value);
                return "Update property into system failed! key = " + key + " ,value = " + value;
            }
            //invalidate cache value
            systemPropertyCache.invalidate(key);

        } catch (Throwable e) {
            log.error("Update property into system failed!", e);
            return "Update property into system failed!" + e;
        }
        return "Update property into system success! key= " + key + "  value = " + value;
    }

}
