package com.higgs.trust.common.dao;

import com.higgs.trust.common.config.rocksdb.RocksDBWrapper;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author tangfashuang
 */
@Component
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

    public static ColumnFamilyHandle getColumnFamilyHandleByName(String columnFamilyName) {
        return rocksDBWrapper.getColumnFamilyHandleMap().get(columnFamilyName);
    }

    @Autowired
    public void setRocksDBWrapper(RocksDBWrapper rocksDBWrapper) {
        RocksUtils.rocksDBWrapper = rocksDBWrapper;
    }
}
