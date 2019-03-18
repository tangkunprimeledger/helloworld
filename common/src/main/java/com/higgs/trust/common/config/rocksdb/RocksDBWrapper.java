package com.higgs.trust.common.config.rocksdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.TransactionDB;

import java.util.Map;

/**
 * @author zhao xiaogang
 * @create 2018-05-21
 */

@Data @AllArgsConstructor public class RocksDBWrapper {
    private TransactionDB rocksDB;
    private Map<String, ColumnFamilyHandle> columnFamilyHandleMap;
}