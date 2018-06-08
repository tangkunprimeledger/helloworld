package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author liuyu
 * @description an agent for data identity snapshot
 * @date 2018-04-09
 */
@Slf4j
@Component
public class DataIdentitySnapshotAgent implements CacheLoader {
    @Autowired
    SnapshotService snapshot;
    @Autowired
    DataIdentityRepository dataIdentityRepository;

    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.DATA_IDENTITY, key);
    }

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    private void put(Object key, Object object) {
        ///snapshot.put(SnapshotBizKeyEnum.DATA_IDENTITY,key,object);
    }

    /**
     * query data identity
     *
     * @param identity
     * @return
     */
    public DataIdentity getDataIdentity(String identity) {
        return get(new DataIdentityCachKey(identity));
    }

    /**
     * save data identity
     *
     * @param dataIdentity
     */
    public void saveDataIdentity(DataIdentity dataIdentity) {
        put(new DataIdentityCachKey(dataIdentity.getIdentity()), dataIdentity);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override
    public Object query(Object object) {
        if (object instanceof DataIdentityCachKey) {
            DataIdentityCachKey key = (DataIdentityCachKey) object;
            return dataIdentityRepository.queryDataIdentity(key.getIdentity());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the method to bachInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean bachInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to bachUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean bachUpdate(Map<Object, Object> updateMap) {
        return false;
    }

    /**
     * the cache key of data identity
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataIdentityCachKey extends BaseBO {
        private String identity;
    }
}
