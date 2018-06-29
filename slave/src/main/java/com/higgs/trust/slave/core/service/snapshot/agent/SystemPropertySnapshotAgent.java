package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SystemProperty snapshot agent
 *
 * @author lingchao
 * @create 2018年06月29日10:41
 */
@Slf4j
@Service
public class SystemPropertySnapshotAgent implements CacheLoader {
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    @Autowired
    private SnapshotService snapshot;

    /**
     * get data from snapshot
     *
     * @param key
     * @param <T>
     * @return
     */
    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.SYSTEM_PROPERTY, key);
    }


    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    public SystemProperty querySystemPropertyByKey(String key) {
        SystemPropertyCacheKey systemPropertyCacheKey = new SystemPropertyCacheKey(key);
        return get(systemPropertyCacheKey);
    }

    @Override
    public Object query(Object object) {
        if (!(object instanceof SystemPropertyCacheKey)) {
            log.error("object {} is not the type of TxOutCacheKey error", object);
            throw new SlaveException(SlaveErrorEnum.SLAVE_SNAPSHOT_DATA_TYPE_ERROR_EXCEPTION);
        }
        SystemPropertyCacheKey systemPropertyCacheKey = (SystemPropertyCacheKey) object;
        return systemPropertyRepository.queryByKey(systemPropertyCacheKey.getKey());
    }

    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        return false;
    }

    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        return false;
    }

    /**
     * SystemPropertyCacheKey
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemPropertyCacheKey extends BaseBO {
        /**
         * key
         */
        private String key;
    }
}
