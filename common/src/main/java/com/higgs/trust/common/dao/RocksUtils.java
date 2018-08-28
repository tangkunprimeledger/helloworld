package com.higgs.trust.common.dao;

import com.higgs.trust.common.config.rocksdb.RocksDBWrapper;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;

public class RocksUtils {
    @Autowired
    private static RocksDBWrapper rocksDBWrapper;

    public static void batchCommit(WriteOptions writeOptions, WriteBatch writeBatch) {
        try {
            rocksDBWrapper.getRocksDB().write(writeOptions, writeBatch);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}
