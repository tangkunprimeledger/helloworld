package com.higgs.trust.common.dao;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.config.rocksdb.RocksDBWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 */
@Component
@Slf4j
public class RocksUtils {

    private static RocksDBWrapper rocksDBWrapper;

    public static void batchCommit(WriteOptions writeOptions, WriteBatch writeBatch) {
        try {
            rocksDBWrapper.getRocksDB().write(writeOptions, writeBatch);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public static Transaction beginTransaction(WriteOptions writeOptions) {
        return rocksDBWrapper.getRocksDB().beginTransaction(writeOptions);
    }

    public static Object getData(String columnFamily, String key) {
        ColumnFamilyHandle columnFamilyHandle = rocksDBWrapper.getColumnFamilyHandleMap().get(columnFamily);
        if (StringUtils.isEmpty(key)) {
            List<Object> objects = new ArrayList<>();
            RocksIterator iterator = rocksDBWrapper.getRocksDB().newIterator(columnFamilyHandle);
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                objects.add(JSON.parse(iterator.value()));
            }
            return objects;
        } else {
            try {
                byte[] keyBytes = JSON.toJSONBytes(key);
                return JSON.parse(rocksDBWrapper.getRocksDB().get(columnFamilyHandle, keyBytes));
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ColumnFamilyHandle getColumnFamilyHandleByName(String name) {
        return rocksDBWrapper.getColumnFamilyHandleMap().get(name);
    }

    public static void txCommit(Transaction tx) {
        try {
            tx.commit();
        } catch (RocksDBException e){
            log.error("transaction commit exception. ", e);
            throw new RuntimeException(e);
        }
    }

    public static void txRollback(Transaction tx) {
        try {
            tx.rollback();
        } catch (RocksDBException e){
            log.error("transaction rollback exception. ", e);
            throw new RuntimeException(e);
        }
    }
    @Autowired
    public void setRocksDBWrapper(RocksDBWrapper rocksDBWrapper) {
        RocksUtils.rocksDBWrapper = rocksDBWrapper;
    }
}
