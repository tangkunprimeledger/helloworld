package com.higgs.trust.slave.core.service.snapshot;

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
}
