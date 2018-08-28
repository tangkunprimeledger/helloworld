package com.higgs.trust.common.dao;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.config.rocksdb.RocksDBWrapper;
import org.apache.commons.lang.ArrayUtils;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseDao including some basic database operation. All sub-dao classes have to extend
 * it and implement the 'getColumnFamilyName' method. The 'getColumnFamilyName' must be
 * contained by the ColumnFamilyDescriptor.
 *
 * @author zhao xiaogang
 * @create 2018-05-21
 */
public abstract class RocksBaseDao<K, V> {

    @Autowired private RocksDBWrapper rocksDBWrapper;

    /**
     * get column family name
     * @return
     */
    protected abstract String getColumnFamilyName();

    public V get(K k) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            byte[] data = rocksDBWrapper.getRocksDB().get(columnFamilyHandle, key);
            if (data != null) {
                return (V)JSON.parse(data);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void put(K k, V v) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            byte[] value = JSON.toJSONBytes(v);
            rocksDBWrapper.getRocksDB().put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void batchPut(WriteBatch batch, K k, V v) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        byte[] key = JSON.toJSONBytes(k);
        byte[] value = JSON.toJSONBytes(v);

        batch.put(columnFamilyHandle, key, value);
    }

    public void delete(K k){
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            rocksDBWrapper.getRocksDB().delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void batchDelete(WriteBatch batch, K k) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        byte[] key = JSON.toJSONBytes(k);
        batch.remove(columnFamilyHandle, key);
    }

    public void batchCommit(WriteOptions options, WriteBatch writeBatch) {
        try {
            rocksDBWrapper.getRocksDB().write(options, writeBatch);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public RocksIterator iterator() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();
        return rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle);
    }

    public List<V> queryAll() {
        RocksIterator iterator = iterator();
        List<V> list = new ArrayList<>();
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            list.add((V)JSON.parse(iterator.value()));
        }
        return list;
    }

    public List<V> queryByPrev(String prev, int limit) {
        RocksIterator iterator = iterator();
        List<V> list = new ArrayList<>();
        byte[] prevByte = JSON.toJSONBytes(prev);
        if (limit < 0) {
            for (iterator.seekForPrev(prevByte); iterator.isValid(); iterator.next()) {
                list.add((V)JSON.parse(iterator.value()));
            }
        } else {
            for (iterator.seekForPrev(prevByte); iterator.isValid() && limit-- > 0; iterator.next()) {
                list.add((V)JSON.parse(iterator.value()));
            }
        }
        return list;
    }

    public List<V> queryByPrev(String prev) {
        return queryByPrev(prev, -1);
    }

    public List<byte[]> keys() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        final List<byte[]> keys = new ArrayList<>();
        final RocksIterator iterator = rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle);
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            keys.add(iterator.key());
        }

        return keys;
    }

    /**
     * Returns a map of keys for which values were found in DB
     * @param keys
     * @return
     * @throws RocksDBException
     */
    public Map<K, V> multiGet(List<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        List<byte[]> keysBytes = new ArrayList<>();
        for (K k : keys) {
            keysBytes.add(SerializationUtils.serialize(k));
        }
        try {
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(keysBytes.size());
            for (int i = 0; i < keysBytes.size(); i++) {
                columnFamilyHandles.add(getColumnFamilyHandle());
            }

            Map<byte[], byte[]> map = rocksDBWrapper.getRocksDB().multiGet(columnFamilyHandles, keysBytes);

            if (!CollectionUtils.isEmpty(map)) {
                Map<K, V> resultMap = new HashMap<>(map.size());
                for (byte[] key : map.keySet()) {
                    byte[] value = map.get(key);
                    if (!ArrayUtils.isEmpty(value)) {
                        resultMap.put((K)JSON.parse(key), (V)JSON.parse(value));
                    }
                }
                return resultMap;
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ColumnFamilyHandle getColumnFamilyHandle() {
        String columnFamilyName = getColumnFamilyName();
        if (StringUtils.isEmpty(columnFamilyName)) {
            throw new IllegalStateException("Column family name can not be empty.");
        }

        Map<String, ColumnFamilyHandle> map = rocksDBWrapper.getColumnFamilyHandleMap();
        ColumnFamilyHandle columnFamilyHandle = map.get(columnFamilyName);
        if (columnFamilyHandle == null) {
            throw new IllegalStateException("Invalid column family name: " + columnFamilyName);
        }

        return columnFamilyHandle;
    }

}
