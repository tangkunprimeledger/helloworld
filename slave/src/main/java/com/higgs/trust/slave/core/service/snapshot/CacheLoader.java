package com.higgs.trust.slave.core.service.snapshot;

import java.util.Map;

/**
 * loading cache service
 *
 * @author lingchao
 * @create 2018年04月10日0:59
 */
public interface CacheLoader {
    /**
     * the method to query object from db
     * @param object
     * @return
     */
    Object query(Object object);

    /**
     * the method to batchInsert data into db
     * @param insertMap
     * @return
     */
    boolean batchInsert(Map<Object, Object> insertMap);

    /**
     * the method to batchUpdate data into db
     * @param updateMap
     * @return
     */
    boolean batchUpdate(Map<Object, Object> updateMap);

}
