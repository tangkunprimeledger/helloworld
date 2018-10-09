package com.higgs.trust.common.config.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration @Slf4j public class RocksDBConfig {

    /**
     * rocks db config properties
     */
    @Value("${trust.rocksdb.file.root:/data/home/admin/trust/rocks/}") private String dbFileRoot;
    @Value("${trust.rocksdb.file.flash.name:trust.db}") private String DB_FILE_FLASH;
    @Value("${trust.rocksdb.file.extra.name:trust-extra.db}") private String DB_FILE_EXTRA;
    @Value("${trust.rocksdb.file.flash.size:10000000000}") private long DB_FILE_FLASH_SIZE;
    @Value("${trust.rocksdb.file.extra.size: 99999999999}") private long DB_FILE_EXTEND_SIZE;
    @Value("${trust.rocksdb.transaction.lockTimeout: 10000}") private long LOCK_TIMEOUT;

    private static List<String> columnFamily;

    static {
        columnFamily = new ArrayList<>();
        columnFamily.add("pendingTransaction");
        columnFamily.add("package");
        columnFamily.add("packageStatus");
        columnFamily.add("block");
        columnFamily.add("transaction");
        columnFamily.add("systemProperty");
        columnFamily.add("rsNode");
        columnFamily.add("policy");
        columnFamily.add("accountDcRecord");
        columnFamily.add("accountDetailFreeze");
        columnFamily.add("accountDetail");
        columnFamily.add("accountFreezeRecord");
        columnFamily.add("accountInfo");
        columnFamily.add("currencyInfo");
        columnFamily.add("ca");
        columnFamily.add("clusterConfig");
        columnFamily.add("config");
        columnFamily.add("systemProperty");
        columnFamily.add("clusterNode");
        columnFamily.add("accountContractBinding");
        columnFamily.add("contract");
        columnFamily.add("contractState");
        columnFamily.add("dataIdentity");
        columnFamily.add("txOut");

        columnFamily.add("blockTest");
        columnFamily.add("tx");

        //rs db
        columnFamily.add("bizType");
        columnFamily.add("coreTransaction");
        columnFamily.add("voteReceipt");
        columnFamily.add("voteRequestRecord");
        columnFamily.add("voteRule");
        columnFamily.add("request");
        columnFamily.add("coreTransactionProcess");

    }

    @Bean public RocksDBWrapper rocksDBWrapper() throws RocksDBException {

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("windows") >= 0) {
            return new RocksDBWrapper(null, null);
        }

        ColumnFamilyOptions options = new ColumnFamilyOptions();

        final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();

        //must have the default column family
        columnFamilyDescriptors
            .add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

        List<ColumnFamilyDescriptor> descriptors =
            columnFamily.stream().map(item -> new ColumnFamilyDescriptor(item.getBytes(), options))
                .collect(Collectors.toList());

        columnFamilyDescriptors.addAll(descriptors);

        final List<DbPath> dbPaths = new ArrayList<>();
        final String flashPath = dbFileRoot + DB_FILE_FLASH;
        final String extraPath = dbFileRoot + DB_FILE_EXTRA;

        dbPaths.add(new DbPath(Paths.get(flashPath), DB_FILE_FLASH_SIZE));
        dbPaths.add(new DbPath(Paths.get(extraPath), DB_FILE_EXTEND_SIZE));

        final DBOptions dbOptions =
            new DBOptions().setDbPaths(dbPaths).setCreateIfMissing(true).setCreateMissingColumnFamilies(true);

        final TransactionDBOptions transactionDBOptions =
            new TransactionDBOptions().setTransactionLockTimeout(LOCK_TIMEOUT);
        List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
        TransactionDB rocksDB = TransactionDB
            .open(dbOptions, transactionDBOptions, dbFileRoot, columnFamilyDescriptors, columnFamilyHandleList);

        int size = columnFamily.size();
        Map<String, ColumnFamilyHandle> columnFamilyHandleMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            columnFamilyHandleMap.put(columnFamily.get(i), columnFamilyHandleList.get(i + 1));
        }

        return new RocksDBWrapper(rocksDB, columnFamilyHandleMap);
    }
}