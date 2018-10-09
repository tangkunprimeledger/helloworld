package com.higgs.trust.common.dao;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.config.rocksdb.RocksDBWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * BaseDao including some basic database operation. All sub-dao classes have to extend
 * it and implement the 'getColumnFamilyName' method. The 'getColumnFamilyName' must be
 * contained by the ColumnFamilyDescriptor.
 *
 * @author zhao xiaogang
 * @create 2018-05-21
 */
public abstract class RocksBaseDao<V> {

    @Autowired private RocksDBWrapper rocksDBWrapper;

    private Class<V> clazz;

    public RocksBaseDao() {
        clazz = getRealType();
    }

    /**
     * get column family name
     *
     * @return
     */
    protected abstract String getColumnFamilyName();

    public V get(String k) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            byte[] data = rocksDBWrapper.getRocksDB().get(columnFamilyHandle, key);
            if (data != null) {
                return JSON.parseObject(data, clazz);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public V getForUpdate(Transaction tx, ReadOptions readOptions, String key, boolean exclusive) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] keyBytes = JSON.toJSONBytes(key);
            byte[] data = tx.getForUpdate(readOptions, columnFamilyHandle, keyBytes, exclusive);
            if (data != null) {
                return JSON.parseObject(data, clazz);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean keyMayExist(String k) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();
        byte[] key = JSON.toJSONBytes(k);
        return rocksDBWrapper.getRocksDB().keyMayExist(columnFamilyHandle, key, new StringBuilder());
    }

    public void put(String k, V v) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            byte[] value = JSON.toJSONBytes(v);
            rocksDBWrapper.getRocksDB().put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String k) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] key = JSON.toJSONBytes(k);
            rocksDBWrapper.getRocksDB().delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public RocksIterator iterator() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();
        return rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle);
    }

    public RocksIterator iterator(ReadOptions ro) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();
        return rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle, ro);
    }

    public List<V> queryAll() {
        RocksIterator iterator = iterator();
        List<V> list = new ArrayList<>();
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            list.add(JSON.parseObject(iterator.value(), clazz));
        }
        return list;
    }

    public List<V> queryByPrefix(String prefix) {
        return queryByPrefix(prefix, -1, null);
    }

    public List<V> queryByPrefix(String prefix, int limit) {
        return queryByPrefix(prefix, limit, null);
    }

    public List<V> queryByPrefix(String prefix, int limit, String position) {
        ReadOptions readOptions = new ReadOptions();
        readOptions.setPrefixSameAsStart(true);
        RocksIterator iterator = iterator(readOptions);
        List<V> list = new ArrayList<>();

        //position represents start pos.
        byte[] prefixByte;
        if (StringUtils.isEmpty(position)) {
            prefixByte = JSON.toJSONBytes(prefix);
        } else {
            prefixByte = JSON.toJSONBytes(position);
        }

        if (limit < 0) {
            for (iterator.seek(prefixByte); iterator.isValid() && isPrefix(prefix, iterator.key()); iterator.next()) {
                list.add(JSON.parseObject(iterator.value(), clazz));
            }
        } else {
            for (iterator.seek(prefixByte);
                 iterator.isValid() && limit-- > 0 && isPrefix(prefix, iterator.key()); iterator.next()) {
                list.add(JSON.parseObject(iterator.value(), clazz));
            }
        }
        return list;
    }

    private boolean isPrefix(String prefix, byte[] key) {
        if (StringUtils.isEmpty(prefix) || ArrayUtils.isEmpty(key)) {
            return false;
        }

        String keyStr = (String)JSON.parse(key);
        if (StringUtils.isEmpty(keyStr)) {
            return false;
        }

        return keyStr.startsWith(prefix);
    }

    public List<String> queryKeysByPrefix(String prefix, int limit) {
        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true).setTotalOrderSeek(true));
        List<String> list = new ArrayList<>();
        byte[] prefixByte = JSON.toJSONBytes(prefix);
        for (iterator.seek(prefixByte); iterator.isValid() && isPrefix(prefix, iterator.key()) && limit-- > 0; iterator.next()) {
            list.add((String)JSON.parse(iterator.key()));
        }
        return list;
    }

    public V queryForPrev(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }

        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true));
        byte[] prefixByte = JSON.toJSONBytes(prefix);
        for (iterator.seekForPrev(prefixByte); iterator.isValid(); ) {
            return JSON.parseObject(iterator.value(), clazz);
        }

        return null;
    }

    public String queryKeyForPrev(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }

        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true));
        byte[] prefixByte = JSON.toJSONBytes(prefix);
        for (iterator.seekForPrev(prefixByte); iterator.isValid(); ) {
            return (String)JSON.parse(iterator.key());
        }

        return null;
    }

    public String queryLastKey() {
        RocksIterator iterator = iterator();
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            return (String)JSON.parse(iterator.key());
        }
        return null;
    }

    public V queryLastValue() {
        RocksIterator iterator = iterator();
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            return JSON.parseObject(iterator.value(), clazz);
        }
        return null;
    }


    public V queryLastValueWithPrefix(String prefix) {
        RocksIterator iterator = iterator();
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            if (!isPrefix(prefix, iterator.key())) {
                return null;
            }
            return JSON.parseObject(iterator.value(), clazz);
        }
        return null;
    }

    public String queryFirstKey(String prefix) {
        RocksIterator iterator = iterator();
        if (StringUtils.isEmpty(prefix)) {
            for (iterator.seekToFirst(); iterator.isValid(); ) {
                return (String)JSON.parse(iterator.key());
            }
        } else {
            byte[] prefixByte = JSON.toJSONBytes(prefix);
            for (iterator.seek(prefixByte); iterator.isValid(); ) {
                if (isPrefix(prefix, iterator.key())) {
                    return (String)JSON.parse(iterator.key());
                }
            }
        }
        return null;
    }

    public V queryFirstValueByPrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }

        byte[] prefixByte = JSON.toJSONBytes(prefix);
        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true));
        for (iterator.seek(prefixByte); iterator.isValid(); iterator.next()) {
            return JSON.parseObject(iterator.value(), clazz);
        }
        return null;
    }

    public V queryMoreThanByPrefixAndPosition(String prefix, String position) {
        byte[] prefixByte = JSON.toJSONBytes(prefix);

        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true));
        for (iterator.seek(prefixByte); iterator.isValid() && isPrefix(prefix, iterator.key()); iterator.next()) {
            String key = (String)JSON.parse(iterator.key());
            if (key.compareTo(position) >= 0) {
                return JSON.parseObject(iterator.value(), clazz);
            }
        }
        return null;
    }

    public List<V> queryLessThanByPrefixAndPosition(String prefix, String position) {
        byte[] prefixByte = JSON.toJSONBytes(prefix);

        List<V> list = new ArrayList<>();
        RocksIterator iterator = iterator(new ReadOptions().setPrefixSameAsStart(true));
        for (iterator.seek(prefixByte); iterator.isValid() && isPrefix(prefix, iterator.key()); iterator.next()) {
            String key = (String)JSON.parse(iterator.key());
            if (key.compareTo(position) <= 0) {
                list.add(JSON.parseObject(iterator.value(), clazz));
            } else {
                break;
            }
        }
        return list;
    }

    public List<String> keys() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        final List<String> keys = new ArrayList<>();
        final RocksIterator iterator = rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle);
        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            keys.add((String)JSON.parse(iterator.key()));
        }

        return keys;
    }

    /**
     * Returns a map of keys for which values were found in DB
     *
     * @param keys
     * @return
     * @throws RocksDBException
     */
    public Map<String, V> multiGet(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        List<byte[]> keysBytes = new ArrayList<>();
        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(keys.size());
        for (String k : keys) {
            keysBytes.add(JSON.toJSONBytes(k));
            columnFamilyHandles.add(getColumnFamilyHandle());
        }
        try {
            Map<byte[], byte[]> map = rocksDBWrapper.getRocksDB().multiGet(columnFamilyHandles, keysBytes);

            if (!CollectionUtils.isEmpty(map)) {
                Map<String, V> resultMap = new HashMap<>(map.size());
                for (byte[] key : map.keySet()) {
                    byte[] value = map.get(key);
                    if (!ArrayUtils.isEmpty(value)) {
                        resultMap.put((String)JSON.parse(key), JSON.parseObject(value, clazz));
                    }
                }
                return resultMap;
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Returns a map of keys for which values were found in DB
     *
     * @param keys
     * @return
     * @throws RocksDBException
     */
    public List<String> multiGetKeys(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        List<byte[]> keysBytes = new ArrayList<>();
        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(keys.size());
        for (String k : keys) {
            keysBytes.add(JSON.toJSONBytes(k));
            columnFamilyHandles.add(getColumnFamilyHandle());
        }

        try {

            Map<byte[], byte[]> map = rocksDBWrapper.getRocksDB().multiGet(columnFamilyHandles, keysBytes);

            if (!CollectionUtils.isEmpty(map)) {
                List<String> existKeys = new ArrayList<>();
                for (byte[] key : map.keySet()) {
                    existKeys.add((String)JSON.parse(key));
                }
                return existKeys;
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteRange(String beginKey, String endKey) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        byte[] beginBytes = JSON.toJSONBytes(beginKey);
        byte[] endBytes = JSON.toJSONBytes(endKey);
        try {
            rocksDBWrapper.getRocksDB().deleteRange(columnFamilyHandle, beginBytes, endBytes);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
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

    public void txPut(Transaction tx, String key, V v) {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

            byte[] keyBytes = JSON.toJSONBytes(key);
            byte[] value = JSON.toJSONBytes(v);
            tx.put(columnFamilyHandle, keyBytes, value);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void txDelete(Transaction tx, String key) {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle();

        byte[] keyBytes = JSON.toJSONBytes(key);
        try {
            tx.delete(columnFamilyHandle, keyBytes);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<V> getRealType() {
        ParameterizedType pt = (ParameterizedType)this.getClass().getGenericSuperclass();
        return (Class<V>)pt.getActualTypeArguments()[0];
    }

    /**
     * show all defined table names
     *
     * @return
     */
    public Set<String> showTables() {
        if (rocksDBWrapper.getColumnFamilyHandleMap() == null) {
            return Collections.emptySet();
        }
        return rocksDBWrapper.getColumnFamilyHandleMap().keySet();
    }

    /**
     * query by prefix 、count and order
     *
     * @param count
     * @param order
     * @return
     */
    public List<V> queryByPrefix(String prefix, int count, int order) {
        RocksIterator iterator = null;
        List<V> list = new ArrayList<>(count);
        int i = 0;
        //desc
        if (order == 0) {
            if (StringUtils.isEmpty(prefix)) {
                iterator = iterator();
                iterator.seekToLast();
            } else {
                ReadOptions readOptions = new ReadOptions();
                readOptions.setPrefixSameAsStart(true);
                iterator = iterator(readOptions);
                iterator.seek(JSON.toJSONBytes(prefix));
            }
            for (;
                 iterator.isValid() && (StringUtils.isEmpty(prefix) ? true : isPrefix(prefix, iterator.key())); iterator
                     .prev()) {
                if (i == count) {
                    break;
                }
                list.add(JSON.parseObject(iterator.value(), clazz));
                i++;
            }
        //asc
        } else if (order == 1) {
            if (StringUtils.isEmpty(prefix)) {
                iterator = iterator();
                iterator.seekToFirst();
            } else {
                ReadOptions readOptions = new ReadOptions();
                readOptions.setPrefixSameAsStart(true);
                iterator = iterator(readOptions);
                iterator.seek(JSON.toJSONBytes(prefix));
            }
            for (;
                 iterator.isValid() && (StringUtils.isEmpty(prefix) ? true : isPrefix(prefix, iterator.key())); iterator
                     .next()) {
                if (i == count) {
                    break;
                }
                list.add(JSON.parseObject(iterator.value(), clazz));
                i++;
            }
        }
        return list;
    }
}
