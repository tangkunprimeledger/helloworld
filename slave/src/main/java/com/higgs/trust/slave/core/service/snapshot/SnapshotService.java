package com.higgs.trust.slave.core.service.snapshot;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;

/**
 * memory SnapshotService interface
 *
 * @author lingchao
 * @create 2018年04月12日21:48
 */
public interface SnapshotService {

    /**
     * start the snapshot transaction.Tag isOpenTransaction to be true.
     */
    void startTransaction();

    /**
     * clear packageCache and txCache
     */
    void clear();

    /**
     * clear globalCache and packageCache and txCache
     */
    void destroy();

    /**
     * get object from snapshot cache .It will get it from txCache first.
     * If there is not object ,then we will get it from db.
     *
     * @param key1
     * @param key2
     * @return
     */
    Object get(SnapshotBizKeyEnum key1, Object key2);

    /**
     * insert  object into the snapshot  txCache
     * @param key1
     * @param key2
     * @param value
     */
    void insert(SnapshotBizKeyEnum key1, Object key2, Object value);

    /**
     * update  object into the snapshot  txCache
     * @param key1
     * @param key2
     * @param value
     */
    void update(SnapshotBizKeyEnum key1, Object key2, Object value);
    /**
     * 1. copy the txCache to packageCache
     * 2.clear txCache
     * 3.tag the isOpenTransaction to be false
     */
    void commit();

    /**
     * 1.clear txCache
     * 2.tag the isOpenTransaction to be false
     */
    void rollback();

    /**
     * 1.flush data into globalCache
     * 2.flush data into db
     */
    void flush();
}
